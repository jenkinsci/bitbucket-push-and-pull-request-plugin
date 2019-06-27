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


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.*;

import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPREventTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;


public class BitBucketPPRPullRequestTriggerMatcher implements BitBucketPPREventTriggerMatcher {
  @Override
  public boolean matchesAction(BitBucketPPREvent bitbucketEvent,
      BitBucketPPRTriggerFilter triggerFilter) {
    if (PULL_REQUEST_APPROVED.equals(bitbucketEvent.getAction())
        && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestApprovedActionFilter) {
      return true;
    }
    if (PULL_REQUEST_UPDATED.equals(bitbucketEvent.getAction())
        && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestUpdatedActionFilter) {
      return true;
    }
    if (PULL_REQUEST_CREATED.equals(bitbucketEvent.getAction())
        && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestCreatedActionFilter) {
      return true;
    }
    if (PULL_REQUEST_MERGED.equals(bitbucketEvent.getAction())
        && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestMergedActionFilter) {
      return true;
    }
    return false;
  }
}
