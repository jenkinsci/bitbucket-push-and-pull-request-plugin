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
import org.apache.commons.lang.NotImplementedException;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;

public class BitBucketPPRPullRequestServerObserver extends BitBucketPPRHandlerTemplate
    implements BitBucketPPRObserver {
  static final Logger LOGGER =
      Logger.getLogger(BitBucketPPRPullRequestServerObserver.class.getName());

  BitBucketPPREventContext context;

  @Override
  public void getNotification(BitBucketPPREvent event) {
    context = event.getContext();
    event.setEventHandler(this);
    event.runHandler();
  }

  @Override
  public void setBuildStatusOnFinished() {
    throw new NotImplementedException();

  }

  @Override
  public void setBuildStatusInProgress() {
    throw new NotImplementedException();
  }
}
