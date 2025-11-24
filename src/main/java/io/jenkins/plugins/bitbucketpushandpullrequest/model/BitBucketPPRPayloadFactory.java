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

package io.jenkins.plugins.bitbucketpushandpullrequest.model;

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_CLOUD_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_CLOUD_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_POST;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_SERVER_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.DIAGNOSTICS;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerPayload;

public class BitBucketPPRPayloadFactory {

  static final Logger logger = Logger.getLogger(BitBucketPPRPayloadFactory.class.getName());

  private BitBucketPPRPayloadFactory() {}

  public static BitBucketPPRPayload getInstance(BitBucketPPRHookEvent bitbucketEvent)
      throws OperationNotSupportedException {

    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())) {
      if (REPOSITORY_CLOUD_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        return new BitBucketPPRCloudPayload();
      }

      // @todo: deprecated. It will be removed in version 3.0.0
      if (REPOSITORY_POST.equalsIgnoreCase(bitbucketEvent.getAction())) {
        logger.warning("Got unexpected old post event, ignored!");
      }

      if (REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        return new BitBucketPPRServerPayload();
      }
    }

    if (PULL_REQUEST_CLOUD_EVENT.equals(bitbucketEvent.getEvent())) {
      return new BitBucketPPRCloudPayload();
    }

    if (PULL_REQUEST_SERVER_EVENT.equals(bitbucketEvent.getEvent())) {
      return new BitBucketPPRServerPayload();
    }

    if (DIAGNOSTICS.equals(bitbucketEvent.getEvent())) {
      return new BitBucketPPRServerPayload();
    }


    throw new OperationNotSupportedException(
        String.format("No processor found for bitbucket event %s.", bitbucketEvent.getEvent()));
  }
}
