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

import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import hudson.model.Result;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientType;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRUtils;

public class BitBucketPPRPushCloudObserver extends BitBucketPPRHandlerTemplate
    implements BitBucketPPRObserver {
  static final Logger logger = Logger.getLogger(BitBucketPPRPushCloudObserver.class.getName());

  private BitBucketPPREventContext context;

  {
    System.setErr(BitBucketPPRUtils.createLoggingProxyForErrors(System.err));
  }
  
  @Override
  public void getNotification(BitBucketPPREvent event) {
    context = event.getContext();
    event.setEventHandler(this);
    event.runHandler();
  }

  @Override
  public void setBuildStatusOnFinished() {
    try {
      BitBucketPPRAction bitbucketAction = context.getAction();
      List<String> commitLinks = bitbucketAction.getCommitLinks();
      int buildNumber = context.getRun().getNumber();
      Result result = context.getRun().getResult();
      String absoluteUrl = context.getAbsoluteUrl();

      for (String link : commitLinks) {
        String url = link + "/statuses/build";
        String payload = "{\"key\": \"" + buildNumber + "\", \"url\": \"" + absoluteUrl + "\", ";
        payload += result == Result.SUCCESS ? "\"state\": \"SUCCESSFUL\""
            : result == Result.ABORTED ? "\"state\": \"STOPPED\"" : "\"state\": \"FAILED\"";
        payload += " }";

        callClient(url, payload);
      }
      logger.info("BB build status was set on 'finished'.");
    } catch (Throwable e) {
      logger.warning("Tried to set BB Build Status on 'finished' using backprogation but " + e.getMessage());
    }
  }

  @Override
  public void setBuildStatusInProgress() {
    try {
      BitBucketPPRAction bitbucketAction = context.getAction();
      List<String> commitLinks = bitbucketAction.getCommitLinks();
      int buildNumber = context.getJobNextBuildNumber();
      String absoluteUrl = context.getJobAbsoluteUrl();

      for (String link : commitLinks) {
        String url = link + "/statuses/build";
        String payload = "{\"key\": \"" + buildNumber + "\", \"url\": \"" + absoluteUrl
            + "\", \"state\": \"INPROGRESS\" }";

        callClient2(url, payload);
      }
      logger.info("BB build status was set on 'in progress'.");
    } catch (Throwable e) {
      logger.warning("Tried to set BB Build Status on 'in progress' using backprogation but " + e.getMessage());
    }
  }

  public void callClient(@Nonnull String url, @Nonnull String payload) throws Throwable {
    BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD,context)
        .send(url, payload);
  }

  public void callClient2(@Nonnull String url, @Nonnull String payload) throws Throwable {
    BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD,context)
        .send2(url, payload);
  }
}
