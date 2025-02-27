/*******************************************************************************
 * The MIT License
 * <p>
 * Copyright (C) 2021, CloudBees, Inc.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import hudson.model.Result;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientType;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;

public class BitBucketPPRPushServerObserver extends BitBucketPPRHandlerTemplate
    implements BitBucketPPRObserver {
  static final Logger logger = Logger.getLogger(BitBucketPPRPushServerObserver.class.getName());

  public BitBucketPPRPushServerObserver() {
    super();
    clientType = BitBucketPPRClientType.SERVER;
  }

  @Override
  public void getNotification(BitBucketPPREvent event) {
    this.context = event.getContext();
    event.setEventHandler(this);
    event.runHandler();
  }

  @Override
  public void setBuildStatusOnFinished() throws MalformedURLException {
    BitBucketPPRAction bitbucketAction = context.getAction();
    Result result = context.getRun().getResult();
    String state = result == Result.SUCCESS ? "SUCCESSFUL" : "FAILED";

    Map<String, String> map = new HashMap<>();
    map.put("key", computeBitBucketBuildKey(context));
    map.put("url", context.getAbsoluteUrl());
    map.put("state", state);

    bitbucketAction.getCommitLinks().forEach(l -> callClient(l, map));
  }

  @Override
  public void setBuildStatusInProgress() throws MalformedURLException {
    BitBucketPPRAction bitbucketAction = context.getAction();
    Map<String, String> map = new HashMap<>();
    map.put("key", computeBitBucketBuildKey(context));
    map.put("url", context.getAbsoluteUrl());
    map.put("state", "INPROGRESS");

    bitbucketAction.getCommitLinks().forEach(l -> callClient(l, map));
  }
}
