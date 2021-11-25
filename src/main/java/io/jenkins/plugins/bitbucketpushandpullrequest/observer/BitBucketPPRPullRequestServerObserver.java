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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import hudson.model.Result;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientType;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;

public class BitBucketPPRPullRequestServerObserver extends BitBucketPPRHandlerTemplate
    implements BitBucketPPRObserver {
  static final Logger logger =
      Logger.getLogger(BitBucketPPRPullRequestServerObserver.class.getName());

  public BitBucketPPRPullRequestServerObserver() {
    super();
    clientType = BitBucketPPRClientType.SERVER;
  }

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

    Result result = context.getRun().getResult();
    BitBucketPPRAction bitbucketAction = context.getAction();

    String url = result == Result.SUCCESS ? bitbucketAction.getLinkApprove()
        : result == Result.FAILURE ? bitbucketAction.getLinkDecline() : null;

    if (url == null) {
      logger.warning("URL for approved not found in Bitbucket payload.");
      return;
    }

    Map<String, String> map = new HashMap<>();
    callClient(url, map);
  }

  @Override
  public void setBuildStatusOnFinished() {
    BitBucketPPRAction bitbucketAction = context.getAction();
    String url = bitbucketAction.getCommitLink();
    Result result = context.getRun().getResult();
    String state = result == Result.SUCCESS ? "SUCCESSFUL" : "FAILED";

    Map<String, String> map = new HashMap<>();
    map.put("key", computeBitBucketBuildKey(context));
    map.put("url", context.getAbsoluteUrl());
    map.put("state", state);

    callClient(url, map);
  }

  @Override
  public void setBuildStatusInProgress() {
    BitBucketPPRAction bitbucketAction = context.getAction();
    String url = bitbucketAction.getCommitLink();

    Map<String, String> map = new HashMap<>();
    map.put("key", computeBitBucketBuildKey(context));
    map.put("url", context.getAbsoluteUrl());
    map.put("state", "INPROGRESS");

    callClient(url, map);
  }
}
