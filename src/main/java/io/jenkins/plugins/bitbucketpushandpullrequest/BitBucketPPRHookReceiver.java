/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc..
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jenkins.plugins.bitbucketpushandpullrequest;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.HOOK_URL;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.USER_AGENT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;

import com.google.gson.Gson;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRNewPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPROldPost;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.processor.BitBucketPPRPayloadProcessor;
import io.jenkins.plugins.bitbucketpushandpullrequest.processor.BitBucketPPRPayloadProcessorFactory;


/**
 * BitbucketHookReceiver which processes HTTP POST packages to $JENKINS_URL/bitbucket-hook
 * 
 * @version 2.0
 */
@Extension
public class BitBucketPPRHookReceiver implements UnprotectedRootAction {

  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRHookReceiver.class.getName());

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

  public void doIndex(StaplerRequest request) throws IOException, OperationNotSupportedException {
    String inputStream = IOUtils.toString(request.getInputStream());

    if (!inputStream.isEmpty() && request.getRequestURI().contains("/" + HOOK_URL + "/")) {
      inputStream = decodeInputStream(inputStream, request.getContentType());

      LOGGER.log(Level.FINE, "Received commit hook notification : {0}", inputStream);

      Gson gson = new Gson();

      BitBucketPPRPayload payload = null;
      BitBucketPPREvent bitbucketEvent = null;

      if (USER_AGENT.equals(request.getHeader("user-agent"))) {
        LOGGER.log(Level.INFO, "Received new x-event-key service payload");
        bitbucketEvent = new BitBucketPPREvent(request.getHeader("x-event-key"));
        payload = gson.fromJson(inputStream, BitBucketPPRNewPayload.class);
      } else {
        LOGGER.log(Level.INFO, "Received old POST service payload");
        bitbucketEvent = new BitBucketPPREvent("repo:post");
        payload = gson.fromJson(inputStream, BitBucketPPROldPost.class);
      }

      BitBucketPPRPayloadProcessor bitbucketPayloadProcessor =
          BitBucketPPRPayloadProcessorFactory.createProcessor(bitbucketEvent);
      bitbucketPayloadProcessor.processPayload(payload);
    } else {
      LOGGER.log(Level.WARNING,
          () -> "The Jenkins job cannot be triggered. You might no have configured "
              + "correctly the WebHook on BitBucket with the last slash "
              + "`http://<JENKINS-URL>/bitbucket-hook/`");
    }
  }

  private String decodeInputStream(String input, String contentType) {
    if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
      try {
        input = URLDecoder.decode(input, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    if (input.startsWith("payload=")) {
      input = input.substring(8);
    }

    return input;
  }

}
