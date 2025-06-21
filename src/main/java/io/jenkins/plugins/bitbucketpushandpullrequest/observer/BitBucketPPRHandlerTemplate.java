/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2021, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.Verb;
import hudson.model.Job;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientType;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventType;

public abstract class BitBucketPPRHandlerTemplate {
  static final Logger logger = Logger.getLogger(BitBucketPPRHandlerTemplate.class.getName());

  protected BitBucketPPREventContext context;
  protected BitBucketPPRClientType clientType;

  public void run(BitBucketPPREventType eventType) throws Exception {
    BitBucketPPRPluginConfig config = getGlobalConfig();
    switch (eventType) {
      case BUILD_STARTED:
        if (config.getNotifyBitBucket()) {
          setBuildStatusInProgress();
        }
        break;
      case BUILD_FINISHED:
        if (config.getNotifyBitBucket()) {
          setBuildStatusOnFinished();
          setApprovedOrDeclined();
        }
        break;
      default:
        throw new Exception();
    }
  }

  // @todo: do we need it also for pushs?
  public void setApprovedOrDeclined() throws MalformedURLException {
  }

  public abstract void setBuildStatusOnFinished() throws MalformedURLException;

  public abstract void setBuildStatusInProgress() throws MalformedURLException;

  protected BitBucketPPRPluginConfig getGlobalConfig() {
    return BitBucketPPRPluginConfig.getInstance();
  }

  protected String computeBitBucketBuildKey(BitBucketPPREventContext context) {
    if (getGlobalConfig().getUseJobNameAsBuildKey()) {
      Job<?, ?> job = context.getRun().getParent();
      return job.getDisplayName();
    } else {
      int buildNumber = context.getBuildNumber();
      return Integer.toString(buildNumber);
    }
  }

  protected void callClient(@Nonnull String url, @Nonnull Map<String, String> payload) {
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      String jsonPayload = objectMapper.writeValueAsString(payload);
      BitBucketPPRClientFactory.createClient(clientType, context).send(url, jsonPayload);
    } catch (JsonProcessingException e) {
      logger.log(Level.WARNING, "Cannot create payload: {0}", e.getMessage());
    } catch (Exception e) {
      logger.warning(e.getMessage());
    }
  }

  protected void callClient(
      @Nonnull Verb verb, @Nonnull String url, @Nonnull Map<String, String> payload) {
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      String jsonPayload = payload.isEmpty() ? "" : objectMapper.writeValueAsString(payload);
      BitBucketPPRClientFactory.createClient(clientType, context).send(verb, url, jsonPayload);
    } catch (JsonProcessingException e) {
      logger.log(Level.WARNING, "Cannot create payload: {0}", e.getMessage());
    } catch (Exception e) {
      logger.warning(e.getMessage());
    }
  }
}
