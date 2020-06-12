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
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.queue.QueueTaskFuture;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientType;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;


public class BitBucketPPRCloudObserver extends BitBucketPPRHandlerTemplate
    implements BitBucketPPRObserver {
  static final Logger LOGGER = Logger.getLogger(BitBucketPPRCloudObserver.class.getName());

  BitBucketPPREventContext context;

  @Override
  public void getNotification(BitBucketPPREvent event) {
    context = event.getContext();
    event.setEventHandler(this);
    event.runHandler();
  }

  @Override
  protected void setApproved() {
    BitBucketPPRAction bitbucketAction = context.getAction();
    SCM scmTrigger = context.getScmTrigger();
    QueueTaskFuture<?> future = context.getFuture();

    try {
      Run<?, ?> run = (Run<?, ?>) future.get();
      Result result = run.getResult();
      LOGGER.info(() -> "Result is " + result);

      GitSCM gitSCM = (GitSCM) scmTrigger;
      UserRemoteConfig config = gitSCM.getUserRemoteConfigs().get(0);
      String url = run.getResult() == Result.SUCCESS ? bitbucketAction.getLinkApprove()
          : bitbucketAction.getLinkDecline();

      if (url != null) {
        String payload = run.getResult() == Result.SUCCESS ? "{ \"approved\": true }"
            : "{ \"approved\": false }";
        BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD, config, run)
            .sendWithUsernamePasswordCredentials(url, payload);
      }
    } catch (NullPointerException e) {
      LOGGER.warning(e.getMessage());
    } catch (InterruptedException e) {
      LOGGER.warning(e.getMessage());
    } catch (Exception e) {
      LOGGER.warning(e.getMessage());
    }

  }

  @Override
  protected void setBuildStatusOnFinished() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void setBuildStatusInProgress() {
    // TODO Auto-generated method stub

  }
}
