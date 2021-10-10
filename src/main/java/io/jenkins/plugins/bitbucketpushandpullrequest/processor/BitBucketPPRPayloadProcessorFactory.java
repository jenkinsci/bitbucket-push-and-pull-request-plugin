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

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_CLOUD_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_CLOUD_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_POST;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_SERVER_PUSH;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;


public final class BitBucketPPRPayloadProcessorFactory {

  static final Logger logger =
      Logger.getLogger(BitBucketPPRPayloadProcessorFactory.class.getName());

  private BitBucketPPRPayloadProcessorFactory() {
    throw new AssertionError();
  }

  public static BitBucketPPRPayloadProcessor createProcessor(final BitBucketPPRHookEvent bitbucketEvent)
      throws OperationNotSupportedException {
    return createProcessor(new BitBucketPPRJobProbe(), bitbucketEvent);
  }

  public static BitBucketPPRPayloadProcessor createProcessor(final BitBucketPPRJobProbe probe,
      final BitBucketPPRHookEvent bitbucketEvent) throws OperationNotSupportedException {

    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())
        && REPOSITORY_CLOUD_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
          logger.info("Create BitBucketPPRRepositoryCloudPayloadProcessor");
      return new BitBucketPPRRepositoryCloudPayloadProcessor(probe, bitbucketEvent);
    }
    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())
        && REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
          logger.info("Create BitBucketPPRRepositoryServerPayloadProcessor");
      return new BitBucketPPRRepositoryServerPayloadProcessor(probe, bitbucketEvent);
    }
    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())
        && REPOSITORY_POST.equalsIgnoreCase(bitbucketEvent.getAction())) {
      logger.warning("Got unexpected old post action, ignored!");
    }

    if (PULL_REQUEST_CLOUD_EVENT.equals(bitbucketEvent.getEvent())) {
      logger.info("Create BitBucketPPRPullRequestCloudPayloadProcessor");
      return new BitBucketPPRPullRequestCloudPayloadProcessor(probe, bitbucketEvent);
    }

    if (PULL_REQUEST_SERVER_EVENT.equals(bitbucketEvent.getEvent())) {
      logger.info("Create BitBucketPPRPullRequestServerPayloadProcessor");
      return new BitBucketPPRPullRequestServerPayloadProcessor(probe, bitbucketEvent);
    }

    throw new OperationNotSupportedException(
        "No processor found for bitbucket event " + bitbucketEvent.getEvent());
  }
}
