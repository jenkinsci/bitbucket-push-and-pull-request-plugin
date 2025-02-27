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

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestServerAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRPayloadPropertyNotFoundException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class BitBucketPPRPullRequestServerPayloadProcessor extends BitBucketPPRPayloadProcessor {

  private static final Logger logger =
      Logger.getLogger(BitBucketPPRPullRequestServerPayloadProcessor.class.getName());

  public BitBucketPPRPullRequestServerPayloadProcessor(
      @Nonnull BitBucketPPRJobProbe jobProbe, @Nonnull BitBucketPPRHookEvent bitbucketEvent) {
    super(jobProbe, bitbucketEvent);
    logger.fine(() -> "Processing " + bitbucketEvent);
  }

  private BitBucketPPRAction buildActionForJobs(
      @Nonnull BitBucketPPRPayload payload, @Nonnull BitBucketPPRHookEvent bitbucketEvent)
      throws BitBucketPPRPayloadPropertyNotFoundException {
    return new BitBucketPPRPullRequestServerAction(payload, bitbucketEvent);
  }

  @Override
  public void processPayload(
      @Nonnull BitBucketPPRPayload payload, BitBucketPPRObservable observable)
      throws BitBucketPPRPayloadPropertyNotFoundException {

    jobProbe.triggerMatchingJobs(
        bitbucketEvent, buildActionForJobs(payload, bitbucketEvent), observable);
  }
}
