/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2025, Christian Del Monte.
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

import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRPayloadPropertyNotFoundException;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;

public abstract class BitBucketPPRPayloadProcessor {
  protected final BitBucketPPRJobProbe jobProbe;
  protected final BitBucketPPRHookEvent bitbucketEvent;

  public BitBucketPPRPayloadProcessor(
      @NonNull final BitBucketPPRJobProbe jobProbe,
      @NonNull final BitBucketPPRHookEvent bitbucketEvent) {
    this.jobProbe = jobProbe;
    this.bitbucketEvent = bitbucketEvent;
  }

  /**
   * Builds the action for the given payload. Constructing the action validates that the payload
   * carries the properties the event requires, so this is the point at which a malformed payload
   * is rejected (with {@link BitBucketPPRPayloadPropertyNotFoundException}). Callers can invoke it
   * before acknowledging the webhook, so a malformed request maps to a 4xx instead of a silent 200.
   */
  public abstract BitBucketPPRAction buildActionForJobs(@NonNull BitBucketPPRPayload payload)
      throws BitBucketPPRPayloadPropertyNotFoundException;

  /** Triggers the jobs matching an already-built action. */
  public void triggerMatchingJobs(
      @NonNull BitBucketPPRAction action, BitBucketPPRObservable observable) {
    jobProbe.triggerMatchingJobs(bitbucketEvent, action, observable);
  }

  /** Builds the action and triggers matching jobs in a single step. */
  public void processPayload(
      @NonNull BitBucketPPRPayload payload, BitBucketPPRObservable observable)
      throws BitBucketPPRPayloadPropertyNotFoundException {
    triggerMatchingJobs(buildActionForJobs(payload), observable);
  }
}
