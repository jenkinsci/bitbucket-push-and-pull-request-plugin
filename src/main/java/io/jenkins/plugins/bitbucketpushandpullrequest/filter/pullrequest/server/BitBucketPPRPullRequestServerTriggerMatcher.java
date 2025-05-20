/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2021, CloudBees, Inc.
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


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server;

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_APPROVED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_COMMENT_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_DECLINED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_MERGED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_SOURCE_UPDATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_UPDATED;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPREventTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;


public class BitBucketPPRPullRequestServerTriggerMatcher
    implements BitBucketPPREventTriggerMatcher {
  @Override
  public boolean matchesAction(final BitBucketPPRHookEvent bitbucketEvent,
      final BitBucketPPRTriggerFilter triggerFilter) {
    return (PULL_REQUEST_SERVER_APPROVED.equalsIgnoreCase(bitbucketEvent.getAction())
        && triggerFilter
            .getActionFilter() instanceof BitBucketPPRPullRequestServerApprovedActionFilter)
        || (PULL_REQUEST_SERVER_UPDATED.equalsIgnoreCase(bitbucketEvent.getAction())
            && triggerFilter
                .getActionFilter() instanceof BitBucketPPRPullRequestServerUpdatedActionFilter)
        || (PULL_REQUEST_SERVER_SOURCE_UPDATED.equalsIgnoreCase(bitbucketEvent.getAction())
            && triggerFilter
                .getActionFilter() instanceof BitBucketPPRPullRequestServerSourceUpdatedActionFilter)
        || (PULL_REQUEST_SERVER_CREATED.equalsIgnoreCase(bitbucketEvent.getAction())
            && triggerFilter
                .getActionFilter() instanceof BitBucketPPRPullRequestServerCreatedActionFilter)
        || (PULL_REQUEST_SERVER_MERGED.equalsIgnoreCase(bitbucketEvent.getAction()) && triggerFilter
            .getActionFilter() instanceof BitBucketPPRPullRequestServerMergedActionFilter)
        || (PULL_REQUEST_SERVER_DECLINED.equalsIgnoreCase(bitbucketEvent.getAction())
            && triggerFilter
                .getActionFilter() instanceof BitBucketPPRPullRequestServerDeclinedActionFilter)
        || (PULL_REQUEST_SERVER_COMMENT_CREATED.equals(bitbucketEvent.getAction()) && triggerFilter
            .getActionFilter() instanceof BitBucketPPRPullRequestServerCommentCreatedActionFilter);
  }
}


