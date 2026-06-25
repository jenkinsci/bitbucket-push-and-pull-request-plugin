/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2025, Christian Del Monte.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package io.jenkins.plugins.bitbucketpushandpullrequest.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.naming.OperationNotSupportedException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.HOOK_URL;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.X_HUB_SIGNATURE;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRPayloadPropertyNotFoundException;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.InputStreamException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayloadFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserverFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.processor.BitBucketPPRPayloadProcessor;
import io.jenkins.plugins.bitbucketpushandpullrequest.processor.BitBucketPPRPayloadProcessorFactory;

/**
 * BitbucketHookReceiver processes HTTP POST requests sent to $JENKINS_URL/bitbucket-hook/
 *
 * @author cdelmonte
 */
@Extension
public class BitBucketPPRHookReceiver extends CrumbExclusion implements UnprotectedRootAction {
  private static final Logger logger = Logger.getLogger(BitBucketPPRHookReceiver.class.getName());

  @POST
  public void doIndex(@Nonnull StaplerRequest request, @Nonnull StaplerResponse response)
      throws IOException {
    // log request URL
    logger.log(Level.INFO, "Request URL: {0}", request.getRequestURI());
    logger.log(Level.INFO, "Internal URL: {0}", getUrlName());
    if (request.getRequestURI().toLowerCase().contains("/" + getUrlName() + "/")
        && request.getMethod().equalsIgnoreCase("POST")) {
      logger.log(Level.INFO, "Received POST request over Bitbucket hook");
      System.out.println(">>> Received POST request over Bitbucket hook");

      try {
        byte[] body = readBody(request);

        // Authenticate the request before doing anything with its content: when a webhook
        // secret is configured, every delivery must carry an X-Hub-Signature header with a
        // valid HMAC-SHA256 of the request body (issue #360). Bitbucket signs the payload
        // before any transport encoding, so the signature is checked on the decompressed,
        // not-yet-URL-decoded body.
        if (BitBucketPPRPluginConfig.getInstance().isWebhookSecretSet()
            && !isSignatureValid(request, body)) {
          writeForbiddenResponse(response);
          return;
        }

        BitBucketPPRHookEvent bitbucketEvent = getBitbucketEvent(request);
        BitBucketPPRPayload payload =
            getPayload(bodyToString(body, request.getContentType()), bitbucketEvent);

        BitBucketPPRPayloadProcessor processor;
        try {
          processor = BitBucketPPRPayloadProcessorFactory.createProcessor(bitbucketEvent);
        } catch (OperationNotSupportedException e) {
          // No processor handles this event (e.g. a diagnostics ping, the deprecated repo:post
          // action, or an unknown/future event whose body still parsed): acknowledge with 200 and
          // stop, preserving the previous behaviour for unsupported-but-harmless events.
          logger.info(
              "No processor for the bitbucket event; acknowledging without processing. "
                  + e.getMessage());
          writeSuccessResponse(response);
          return;
        }

        // Build (and thereby validate) the action BEFORE acknowledging the webhook, so a
        // malformed or unusable payload is rejected with a 4xx instead of a silent HTTP 200
        // followed by a dropped job (issue #384). Any failure to build the action -- a missing
        // property (BitBucketPPRPayloadPropertyNotFoundException) or an unparseable field that
        // surfaces as a RuntimeException (e.g. an unparseable repository/clone URL) -- maps to a
        // 400 here, before the acknowledgement.
        BitBucketPPRAction action;
        try {
          action = processor.buildActionForJobs(payload);
        } catch (BitBucketPPRPayloadPropertyNotFoundException e) {
          // The payload is missing a property the event requires (descriptive validation).
          logger.log(Level.WARNING,
              "The webhook payload is missing a required property; the request is rejected. It "
                  + "could also be that the Bitbucket Client / Server version is not currently "
                  + "supported by the plugin.",
              e);
          writeFailResponse(response);
          return;
        } catch (RuntimeException e) {
          // Safety net: the action could not be built from this payload for some other reason
          // (e.g. an unparseable repository/clone URL). Reject rather than ack a request we cannot
          // act on; this is NOT necessarily a missing property.
          logger.log(Level.WARNING,
              "The webhook payload could not be converted into a usable action; the request is "
                  + "rejected.",
              e);
          writeFailResponse(response);
          return;
        }

        writeSuccessResponse(response);

        // The observable is only needed for job triggering. At this point the request has already
        // been validated and acknowledged; anything from here on (observable creation, job
        // matching/notification) is internal post-ack processing and keeps the previous semantics
        // of failing after the 200 rather than affecting the acknowledged response.
        BitBucketPPRObservable observable =
            BitBucketPPRObserverFactory.createObservable(bitbucketEvent);
        processor.triggerMatchingJobs(action, observable);
      } catch (IOException
          | InputStreamException
          | JsonSyntaxException
          | OperationNotSupportedException e) {
        System.out.println(">>> Exception: " + e.getMessage());
        writeFailResponse(response);
      }
    }
  }

  private void writeSuccessResponse(@Nonnull StaplerResponse response) throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);
    PrintWriter out = response.getWriter();
    out.write("Bitbucket PPR Plugin: request received successfully.");
    out.flush();
    out.close();
  }

  private void writeFailResponse(@Nonnull StaplerResponse response) throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    PrintWriter out = response.getWriter();
    out.write("Bitbucket PPR Plugin: request failed.");
    out.flush();
    out.close();
  }

  private void writeForbiddenResponse(@Nonnull StaplerResponse response) throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    PrintWriter out = response.getWriter();
    out.write("Bitbucket PPR Plugin: request rejected: missing or invalid signature.");
    out.flush();
    out.close();
  }

  /**
   * Reads the raw request body, transparently gunzipping it when the request was delivered with
   * {@code Content-Encoding: gzip}. The bytes are returned before any URL decoding because the
   * webhook signature, when present, is computed over exactly these bytes.
   */
  byte[] readBody(@Nonnull StaplerRequest request) throws IOException {
    boolean gzipHeader = "gzip".equalsIgnoreCase(request.getHeader("Content-Encoding"));
    try (InputStream body = gzipHeader ? GzipUtils.maybeGunzip(request.getInputStream())
        : request.getInputStream()) {
      return IOUtils.toByteArray(body);
    }
  }

  String bodyToString(@Nonnull byte[] body, String contentType) throws InputStreamException {
    String inputStream = new String(body, StandardCharsets.UTF_8);
    if (StringUtils.isBlank(inputStream)) {
      logger.severe("The Jenkins job cannot be triggered. The input stream is empty.");
      throw new InputStreamException("The input stream is empty");
    }
    return decodeInputStream(inputStream, contentType);
  }

  /**
   * Validates the {@code X-Hub-Signature} header against the configured webhook secret. Returns
   * false -- rejecting the request -- when the configured secret credential cannot be resolved
   * (fail closed) or when the signature is missing, malformed or does not match.
   */
  private boolean isSignatureValid(@Nonnull StaplerRequest request, @Nonnull byte[] body) {
    String secret = BitBucketPPRPluginConfig.getInstance().getWebhookSecret();
    if (StringUtils.isBlank(secret)) {
      logger.severe(
          "A webhook secret credential is configured but could not be resolved to a non-empty "
              + "secret; the request is rejected.");
      return false;
    }
    if (!SignatureUtils.isValid(body, request.getHeader(X_HUB_SIGNATURE), secret)) {
      logger.warning(
          "The webhook request signature is missing or does not match the configured secret; "
              + "the request is rejected.");
      return false;
    }
    return true;
  }

  BitBucketPPRPayload getPayload(
      @Nonnull final String inputStream, @Nonnull BitBucketPPRHookEvent bitbucketEvent)
      throws JsonSyntaxException, OperationNotSupportedException {
    BitBucketPPRPayload pl =
        new Gson()
            .fromJson(
                inputStream, BitBucketPPRPayloadFactory.getInstance(bitbucketEvent).getClass());
    if (pl == null) {
      logger.severe(
          "The Jenkins job cannot be triggered. The request body could not be deserialized "
              + "into a usable Bitbucket payload (got null). The body is malformed, truncated, "
              + "or does not match the expected shape for this event.");
      throw new JsonSyntaxException("The deserialized Bitbucket payload is null.");
    }
    logger.log(Level.FINEST, "the payload is: {0}", pl);
    return pl;
  }

  static String decodeInputStream(
      @Nonnull final String inputStream, @Nonnull final String contentType) {
    String input = inputStream;
    if (StringUtils.startsWithIgnoreCase(
        contentType, BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED)) {
      input = URLDecoder.decode(input, StandardCharsets.UTF_8);
    }
    if (StringUtils.startsWithIgnoreCase(input, BitBucketPPRConst.PAYLOAD_PFX)) {
      input = StringUtils.removeStartIgnoreCase(input, BitBucketPPRConst.PAYLOAD_PFX);
    }
    return input;
  }

  BitBucketPPRHookEvent getBitbucketEvent(@Nonnull StaplerRequest request)
      throws OperationNotSupportedException {
    String xEventHeader = request.getHeader(BitBucketPPRConst.X_EVENT_KEY);

    // @todo: DEPRECATED_X_HEADER_REPO_POST deprecated. It will be removed in version 3.0.0
    return StringUtils.isNotBlank(xEventHeader)
        ? new BitBucketPPRHookEvent(xEventHeader)
        : new BitBucketPPRHookEvent(BitBucketPPRConst.DEPRECATED_X_HEADER_REPO_POST);
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "Processing Bitbucket request.";
  }

  @Override
  public String getUrlName() {
    // Resolved per call rather than cached in a static field: a static would pin the
    // GlobalConfiguration instance of whichever Jenkins was alive when this class loaded,
    // going stale across Jenkins instances (e.g. JenkinsRule tests in the same JVM).
    BitBucketPPRPluginConfig globalConfig = BitBucketPPRPluginConfig.getInstance();
    return (globalConfig.isHookUrlSet() ? globalConfig.getHookUrl() : HOOK_URL).toLowerCase();
  }

  @Override
  public boolean process(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    String path = request.getPathInfo();

    if (StringUtils.isNotBlank(path) && path.equals("/" + getUrlName() + "/")) {
      chain.doFilter(request, response);

      return true;
    }

    return false;
  }
}
