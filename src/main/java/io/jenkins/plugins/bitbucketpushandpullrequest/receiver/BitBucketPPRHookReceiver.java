/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2022, CloudBees, Inc.
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

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.HOOK_URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.InputStreamException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayloadFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserverFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.processor.BitBucketPPRPayloadProcessorFactory;

/**
 * BitbucketHookReceiver processes HTTP POST requests sent to $JENKINS_URL/bitbucket-hook/
 * 
 * @author cdelmonte
 * 
 */
@Extension
public class BitBucketPPRHookReceiver implements UnprotectedRootAction {
  private static final Logger logger = Logger.getLogger(BitBucketPPRHookReceiver.class.getName());
  private static final BitBucketPPRPluginConfig globalConfig =
      BitBucketPPRPluginConfig.getInstance();

  public void doIndex(@Nonnull StaplerRequest request, @Nonnull StaplerResponse response)
      throws IOException {
    if (request.getRequestURI().toLowerCase().contains("/" + getUrlName() + "/")
        && request.getMethod().equalsIgnoreCase("POST")) {
      logger.log(Level.INFO, "Received POST request over Bitbucket hook");

      try { 
        BitBucketPPRHookEvent bitbucketEvent = getBitbucketEvent(request);    
        BitBucketPPRPayload payload = getPayload(getInputStream(request), bitbucketEvent);
        BitBucketPPRObservable observable = BitBucketPPRObserverFactory.createObservable(bitbucketEvent);
        
        writeSuccessResponse(response, "Bitbuckt PPR Plugin: request received successfully.");
        
        BitBucketPPRPayloadProcessorFactory.createProcessor(bitbucketEvent).processPayload(payload, observable);
      } catch (IOException | InputStreamException | JsonSyntaxException | OperationNotSupportedException e) {
        writeFailResponse(response, "Bitbuckt PPR Plugin: request failed.");
      }
    }
  }

  private void writeSuccessResponse(@Nonnull StaplerResponse response, @Nonnull String msg)
      throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);
    PrintWriter out = response.getWriter();
    out.write(msg);
    out.flush();
    out.close();
  }

  private void writeFailResponse(@Nonnull StaplerResponse response, @Nonnull String msg)
      throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    PrintWriter out = response.getWriter();
    out.write(msg);
    out.flush();
    out.close();
  }

  String getInputStream(@Nonnull StaplerRequest request) throws IOException, InputStreamException {
    String inputStream = IOUtils.toString(request.getInputStream());
    if (StringUtils.isBlank(inputStream)) {
      logger.severe("The Jenkins job cannot be triggered. The input stream is empty.");
      throw new InputStreamException("The input stream is empty");
    }
    return decodeInputStream(inputStream, request.getContentType());
  }

  BitBucketPPRPayload getPayload(@Nonnull final String inputStream,
      @Nonnull BitBucketPPRHookEvent bitbucketEvent)
      throws JsonSyntaxException, OperationNotSupportedException {
    BitBucketPPRPayload pl = new Gson().fromJson(inputStream,
        BitBucketPPRPayloadFactory.getInstance(bitbucketEvent).getClass());
    logger.log(Level.FINEST, "the payload is: {0}", pl);
    return pl;
  }

  static String decodeInputStream(@Nonnull final String inputStream,
      @Nonnull final String contentType) throws UnsupportedEncodingException {
    String input = inputStream;
    if (StringUtils.startsWithIgnoreCase(contentType,
        BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED)) {
      input = URLDecoder.decode(input, StandardCharsets.UTF_8.toString());
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
    return StringUtils.isNotBlank(xEventHeader) ? new BitBucketPPRHookEvent(xEventHeader)
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
}
