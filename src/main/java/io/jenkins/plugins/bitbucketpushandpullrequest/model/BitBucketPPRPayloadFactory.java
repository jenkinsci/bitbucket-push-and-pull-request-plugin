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

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.*;

import javax.naming.OperationNotSupportedException;

import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.old.BitBucketPPROldPost;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerPayload;


public class BitBucketPPRPayloadFactory {

  private BitBucketPPRPayloadFactory() {}

  public static BitBucketPPRPayload getInstance(BitBucketPPREvent bitbucketEvent)
      throws OperationNotSupportedException {
    BitBucketPPRPayload payload = null;

    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())) {
      if (REPOSITORY_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPRNewPayload();
      } else if (REPOSITORY_POST.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPROldPost();
      } else if (REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPRServerPayload();
      }
    } else if (PULL_REQUEST_EVENT.equals(bitbucketEvent.getEvent())) {
      payload = new BitBucketPPRNewPayload();
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
