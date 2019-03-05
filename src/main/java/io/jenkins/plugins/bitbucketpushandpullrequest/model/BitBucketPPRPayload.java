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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRActor;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRApproval;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRPullRequest;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRPush;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRRepository;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.old.BitBucketPPROldRepository;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerActor;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerChange;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerRepository;


public interface BitBucketPPRPayload extends Serializable {
  default BitBucketPPRRepository getRepository() {
    return null;
  }

  default BitBucketPPRPullRequest getPullRequest() {
    return null;
  }

  default BitBucketPPRPush getPush() {
    return null;
  }

  default BitBucketPPRActor getActor() {
    return null;
  }

  default BitBucketPPRApproval getApproval() {
    return null;
  }

  default BitBucketPPROldRepository getOldRepository() {
    return null;
  }

  default String getCanonUrl() {
    return null;
  }

  default String getUser() {
    return null;
  }

  default BitBucketPPRServerActor getServerActor() {
    return null;
  }

  default BitBucketPPRServerRepository getServerRepository() {
    return null;
  }

  default List<BitBucketPPRServerChange> getServerChanges() {
    return new ArrayList<>();
  }
}
