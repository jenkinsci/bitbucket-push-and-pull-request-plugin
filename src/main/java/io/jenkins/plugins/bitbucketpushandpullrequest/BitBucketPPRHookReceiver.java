/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
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


package io.jenkins.plugins.bitbucketpushandpullrequest;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.HOOK_URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayloadFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.processor.BitBucketPPRPayloadProcessor;
import io.jenkins.plugins.bitbucketpushandpullrequest.processor.BitBucketPPRPayloadProcessorFactory;


/**
 * BitbucketHookReceiver which processes HTTP POST packages to $JENKINS_URL/bitbucket-hook
 * 
 * @version 2.0
 */
@Extension
public class BitBucketPPRHookReceiver implements UnprotectedRootAction {

  private static final Logger logger = Logger.getLogger(BitBucketPPRHookReceiver.class.getName());

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
    return HOOK_URL;
  }

  public void doIndex(final StaplerRequest request, final StaplerResponse response)
      throws IOException {
    String inputStream = IOUtils.toString(request.getInputStream());

    if (inputStream.isEmpty()) {
      logger.warning("The Jenkins job cannot be triggered. The input stream is empty.");
    } else if (request.getRequestURI().contains("/" + HOOK_URL + "/")) {
      logger.log(Level.FINE, "Received commit hook notification : {0}", inputStream);

      inputStream = decodeInputStream(inputStream, request.getContentType());
      BitBucketPPREvent bitbucketEvent = null;
      if (request.getHeader("x-event-key") != null) {
        try {
          bitbucketEvent = new BitBucketPPREvent(request.getHeader("x-event-key"));
          logger.log(Level.INFO,
              () -> "Received x-event-key " + request.getHeader("x-event-key") + " from Bitbucket");
        } catch (final OperationNotSupportedException e) {
          logger.warning(e.getMessage());
        }
      } else {
        logger.log(Level.INFO, "Received old POST payload. (Deprecated, it will be removed.)");
        try {
          bitbucketEvent = new BitBucketPPREvent("repo:post");
        } catch (final OperationNotSupportedException e) {
          logger.warning(e.getMessage());
        }
      }

      if (bitbucketEvent != null) {
        final Gson gson = new Gson();
        try {
          final BitBucketPPRPayload payload = gson.fromJson(inputStream,
              BitBucketPPRPayloadFactory.getInstance(bitbucketEvent).getClass());
          logger.fine(() -> "the payload is: " + payload.toString());

          final BitBucketPPRPayloadProcessor bitbucketPayloadProcessor =
              BitBucketPPRPayloadProcessorFactory.createProcessor(bitbucketEvent);
          logger.fine(
              () -> "the selected payload processor is: " + bitbucketPayloadProcessor.toString());

          bitbucketPayloadProcessor.processPayload(payload);
        } catch (final Exception e) {
          logger.warning(e.getMessage());
        }
      } else {
        logger.warning("the BitbucketEvent is null.");
      }

      logger.log(Level.FINE, "Sending response.");
      try {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        final PrintWriter out = response.getWriter();
        out.write("ok");
        out.flush();
        out.close();
        logger.log(Level.INFO, "Response sent.");
      } catch (final Exception e) {
        logger.warning(e.getMessage());
      }
    } else {
      logger.log(Level.WARNING,
          () -> "The Jenkins job cannot be triggered. You might no have configured "
              + "correctly the WebHook on BitBucket with the last slash "
              + "`http://<JENKINS-URL>/bitbucket-hook/`");
    }
  }

  private String decodeInputStream(final String inputStream, final String contentType) {
    String input = inputStream;

    if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
      try {
        input = URLDecoder.decode(input, "UTF-8");
      } catch (final UnsupportedEncodingException e) {
        logger.warning(e.getMessage());
      }
    }

    if (input.startsWith("payload=")) {
      input = input.substring(8);
    }

    return input;
  }
}
