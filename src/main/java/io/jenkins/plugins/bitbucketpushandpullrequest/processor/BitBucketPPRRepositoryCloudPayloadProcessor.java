/*******************************************************************************
 * The MIT License
 * <p>
 * Copyright (C) 2020, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import java.util.logging.Logger;
import javax.annotation.Nonnull;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;


public class BitBucketPPRRepositoryCloudPayloadProcessor extends BitBucketPPRPayloadProcessor {
  static final Logger logger =
      Logger.getLogger(BitBucketPPRRepositoryCloudPayloadProcessor.class.getName());

  public BitBucketPPRRepositoryCloudPayloadProcessor(@Nonnull BitBucketPPRJobProbe jobProbe,
      @Nonnull BitBucketPPRHookEvent bitbucketEvent) {
    super(jobProbe, bitbucketEvent);
  }

  private BitBucketPPRAction buildActionForJobs(@Nonnull BitBucketPPRPayload payload) {
    logger.info("Instantiate BitBucketPPRRepositoryAction");
    return new BitBucketPPRRepositoryAction(payload);
  }

  @Override
  public void processPayload(@Nonnull BitBucketPPRPayload payload,
      BitBucketPPRObservable observable) {
    BitBucketPPRAction action = buildActionForJobs(payload);
    jobProbe.triggerMatchingJobs(bitbucketEvent, action, observable);
  }
}
