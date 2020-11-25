/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2020, CloudBees, Inc.
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

import java.util.logging.Logger;
import javax.annotation.Nonnull;
import hudson.model.Result;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientType;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;

public class BitBucketPPRPullRequestCloudObserver extends BitBucketPPRHandlerTemplate implements BitBucketPPRObserver {
  static final Logger logger = Logger.getLogger(BitBucketPPRPullRequestCloudObserver.class.getName());

  BitBucketPPREventContext context;

  @Override
  public void getNotification(BitBucketPPREvent event) {
    context = event.getContext();
    event.setEventHandler(this);
    event.runHandler();
  }

  @Override
  public void setApproved() {
    if (!context.getFilter().shouldSendApprove()) {
      return;
    }

    try {
      Result result = context.getRun().getResult();
      logger.info(() -> "The result is " + result);

      BitBucketPPRAction bitbucketAction = context.getAction();
      String url = null;
      String payload = null;
      if (result == Result.SUCCESS) {
        url = bitbucketAction.getLinkApprove();
        payload = "{ \"approved\": true }";
      } else if (result == Result.FAILURE) {
        url = bitbucketAction.getLinkDecline();
        payload = "{ \"approved\": false }";
      }
      if (url == null) {
        logger.info(() -> "Cannot set URL for approved.");
        return;
      }

      BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD, context).send(url, payload);

    } catch (NullPointerException e) {
      logger.warning(e.getMessage());
    } catch (InterruptedException e) {
      logger.warning(e.getMessage());
    } catch (Exception e) {
      logger.warning(e.getMessage());
    }
  }

  @Override
  public void setBuildStatusOnFinished() {
    BitBucketPPRAction bitbucketAction = context.getAction();
    String url = bitbucketAction.getCommitLink() + "/statuses/build";
    Result result = context.getRun().getResult();

    String payload = "{\"key\": \"" + context.getRun().getNumber() + "\", \"url\": \"" + context.getAbsoluteUrl()
        + "\", ";
    payload += result == Result.SUCCESS ? "\"state\": \"SUCCESSFUL\""
        : result == Result.ABORTED ? "\"state\": \"STOPPED\"" : "\"state\": \"FAILED\"";
    payload += " }";

    callClient(url, payload);
  }

  @Override
  public void setBuildStatusInProgress() {
    BitBucketPPRAction bitbucketAction = context.getAction();
    String url = bitbucketAction.getCommitLink() + "/statuses/build";
    int buildNumber = context.getJobNextBuildNumber();
    String absoluteUrl = context.getJobAbsoluteUrl();

    String payload = "{\"key\": \"" + buildNumber + "\", \"url\": \"" + absoluteUrl + "\", \"state\": \"INPROGRESS\" }";

    callClient2(url, payload);
  }

  private void callClient(@Nonnull String url, @Nonnull String payload) {
    try {
      BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD, context).send(url, payload);
    } catch (Exception e) {
      logger.warning(e.getMessage());
    }
  }
  
  private void callClient2(@Nonnull String url, @Nonnull String payload) {
    try {
      BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD, context).send2(url, payload);
    } catch (Exception e) {
      logger.warning(e.getMessage());
    }
  }
}
