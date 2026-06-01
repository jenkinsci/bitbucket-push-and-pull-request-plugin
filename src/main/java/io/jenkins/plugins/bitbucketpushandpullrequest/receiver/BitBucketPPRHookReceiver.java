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
  private static final BitBucketPPRPluginConfig globalConfig =
      BitBucketPPRPluginConfig.getInstance();

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
        BitBucketPPRHookEvent bitbucketEvent = getBitbucketEvent(request);
        BitBucketPPRPayload payload = getPayload(getInputStream(request), bitbucketEvent);
        BitBucketPPRObservable observable =
            BitBucketPPRObserverFactory.createObservable(bitbucketEvent);

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
    out.write("Bitbuckt PPR Plugin: request received successfully.");
    out.flush();
    out.close();
  }

  private void writeFailResponse(@Nonnull StaplerResponse response) throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    PrintWriter out = response.getWriter();
    out.write("Bitbuckt PPR Plugin: request failed.");
    out.flush();
    out.close();
  }

  String getInputStream(@Nonnull StaplerRequest request) throws IOException, InputStreamException {
    boolean gzipHeader = "gzip".equalsIgnoreCase(request.getHeader("Content-Encoding"));
    try (InputStream body = gzipHeader ? GzipUtils.maybeGunzip(request.getInputStream())
        : request.getInputStream()) {
      String inputStream = IOUtils.toString(body, StandardCharsets.UTF_8);
      if (StringUtils.isBlank(inputStream)) {
        logger.severe("The Jenkins job cannot be triggered. The input stream is empty.");
        throw new InputStreamException("The input stream is empty");
      }
      return decodeInputStream(inputStream, request.getContentType());
    }
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
