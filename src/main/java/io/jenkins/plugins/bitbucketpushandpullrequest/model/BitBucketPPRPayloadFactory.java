/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018, CloudBees, Inc.
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

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.PULL_REQUEST_CLOUD_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.PULL_REQUEST_SERVER_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_POST;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_CLOUD_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_SERVER_PUSH;

import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerPayload;

public class BitBucketPPRPayloadFactory {

  static final Logger logger = Logger.getLogger(BitBucketPPRPayloadFactory.class.getName());

  private BitBucketPPRPayloadFactory() {}

  public static BitBucketPPRPayload getInstance(BitBucketPPRHookEvent bitbucketEvent)
      throws OperationNotSupportedException {
    BitBucketPPRPayload payload = null;

    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())) {
      if (REPOSITORY_CLOUD_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPRCloudPayload();
      } else if (REPOSITORY_POST.equalsIgnoreCase(bitbucketEvent.getAction())) {
        logger.warning("Got unexpected old post event, ignored!");
      } else if (REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPRServerPayload();
      }
    } else if (PULL_REQUEST_CLOUD_EVENT.equals(bitbucketEvent.getEvent())) {
      payload = new BitBucketPPRCloudPayload();
    } else if (PULL_REQUEST_SERVER_EVENT.equals(bitbucketEvent.getEvent())) {
      payload = new BitBucketPPRServerPayload();
    }

    if (payload == null) {
      throw new OperationNotSupportedException(
          "No processor found for bitbucket event " + bitbucketEvent.getEvent());
    }

    return payload;
  }
}
