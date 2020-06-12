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
package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import java.util.List;
import javax.annotation.Nonnull;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserver;


public class BitBucketPPRPullRequestCloudPayloadProcessor extends BitBucketPPRPayloadProcessor {
  public BitBucketPPRPullRequestCloudPayloadProcessor(@Nonnull BitBucketPPRJobProbe jobProbe,
      @Nonnull BitBucketPPREvent bitbucketEvent) {
    super(jobProbe, bitbucketEvent);
  }

  private BitBucketPPRAction buildActionForJobs(@Nonnull BitBucketPPRPayload payload) {
    return new BitBucketPPRPullRequestAction(payload);
  }

  @Override
  public void processPayload(@Nonnull BitBucketPPRPayload payload, List<BitBucketPPRObserver> observers) {
    BitBucketPPRAction action = buildActionForJobs(payload);
    jobProbe.triggerMatchingJobs(bitbucketEvent, action, observers);
  }
}
