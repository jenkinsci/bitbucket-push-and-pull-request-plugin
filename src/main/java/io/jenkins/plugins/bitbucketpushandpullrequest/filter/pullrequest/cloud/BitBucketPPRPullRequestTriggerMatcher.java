/*******************************************************************************
 * The MIT License
 * <p>
 * Copyright (C) 2021, CloudBees, Inc.
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


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud;


import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_APPROVED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_COMMENT_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_COMMENT_DELETED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_COMMENT_UPDATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_DECLINED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_MERGED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_UPDATED;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPREventTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;


public class BitBucketPPRPullRequestTriggerMatcher implements BitBucketPPREventTriggerMatcher {
  @Override
  public boolean matchesAction(BitBucketPPRHookEvent bitbucketEvent,
      BitBucketPPRTriggerFilter triggerFilter) {

    return PULL_REQUEST_APPROVED.equals(bitbucketEvent.getAction())
        && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestApprovedActionFilter
        || PULL_REQUEST_UPDATED.equals(bitbucketEvent.getAction())
            && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestUpdatedActionFilter
        || PULL_REQUEST_CREATED.equals(bitbucketEvent.getAction())
            && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestCreatedActionFilter
        || PULL_REQUEST_COMMENT_CREATED.equals(bitbucketEvent.getAction()) && triggerFilter
            .getActionFilter() instanceof BitBucketPPRPullRequestCommentCreatedActionFilter
        || PULL_REQUEST_COMMENT_UPDATED.equals(bitbucketEvent.getAction()) && triggerFilter
            .getActionFilter() instanceof BitBucketPPRPullRequestCommentUpdatedActionFilter
        || PULL_REQUEST_COMMENT_DELETED.equals(bitbucketEvent.getAction()) && triggerFilter
            .getActionFilter() instanceof BitBucketPPRPullRequestCommentDeletedActionFilter
        || PULL_REQUEST_MERGED.equals(bitbucketEvent.getAction())
            && triggerFilter.getActionFilter() instanceof BitBucketPPRPullRequestMergedActionFilter
        || PULL_REQUEST_DECLINED.equals(bitbucketEvent.getAction()) && triggerFilter
            .getActionFilter() instanceof BitBucketPPRPullRequestDeclinedActionFilter;
  }
}
