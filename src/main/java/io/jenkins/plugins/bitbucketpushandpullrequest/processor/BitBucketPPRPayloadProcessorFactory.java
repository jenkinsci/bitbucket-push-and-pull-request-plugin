/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.*;

import javax.naming.OperationNotSupportedException;

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPREvent;


public final class BitBucketPPRPayloadProcessorFactory {


  private BitBucketPPRPayloadProcessorFactory() {
    throw new AssertionError();
  }

  public static BitBucketPPRPayloadProcessor createProcessor(BitBucketPPREvent bitbucketEvent)
      throws OperationNotSupportedException {
    return createProcessor(new BitBucketPPRJobProbe(), bitbucketEvent);
  }


  public static BitBucketPPRPayloadProcessor createProcessor(BitBucketPPRJobProbe probe,
      BitBucketPPREvent bitbucketEvent) throws OperationNotSupportedException {

    BitBucketPPRPayloadProcessor processor = null;

    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())) {
      if (REPOSITORY_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        processor = new BitBucketPPRRepositoryPayloadProcessor(probe, bitbucketEvent);
      } else if (REPOSITORY_POST.equalsIgnoreCase(bitbucketEvent.getAction())) {
        processor = new BitBucketPPROldPostPayloadProcessor(probe, bitbucketEvent);
      } else if (REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        processor = new BitBucketPPRRepositoryServerPayloadProcessor(probe, bitbucketEvent);
      }
    } else if (PULL_REQUEST_EVENT.equals(bitbucketEvent.getEvent())) {
      processor = new BitBucketPPRPullRequestPayloadProcessor(probe, bitbucketEvent);
    }

    if (processor == null) {
      throw new OperationNotSupportedException(
          "No processor found for bitbucket event " + bitbucketEvent.getEvent());
    }

    return processor;
  }
}
