/*******************************************************************************
 * The MIT License
 * <p>
 * Copyright (C) 2018, CloudBees, Inc.
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


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import java.util.logging.Level;
import java.util.logging.Logger;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPREventTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;


public class BitBucketPPRRepositoryTriggerMatcher implements BitBucketPPREventTriggerMatcher {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRRepositoryTriggerMatcher.class.getName());

  @Override
  public boolean matchesAction(BitBucketPPRHookEvent bitbucketEvent,
      BitBucketPPRTriggerFilter triggerFilter) {
    logger.log(Level.INFO, bitbucketEvent::toString);


    logger.log(Level.FINE,
        "1. (Is the trigger filter instance of BitBucketPPRRepositoryPushActionFilter? <<{0}>> "
            + "AND does it equal BitBucketPPRConsts.REPOSITORY_PUSH {1} to bitbucketEvent.getAction() {2}? <<{3}>>) OR "
            + "2. (Is the trigger filter instance of BitBucketPPRServerRepositoryPushActionFilter? <<{4}>>"
            + "AND does it equal BitBucketPPRConsts.REPOSITORY_SERVER_PUSH {5} to bitbucketEvent.getAction() {6}? <<{7}>> ",
        new Object[] {
            triggerFilter.getActionFilter() instanceof BitBucketPPRRepositoryPushActionFilter,
            BitBucketPPRConst.REPOSITORY_CLOUD_PUSH, bitbucketEvent.getAction(),
            BitBucketPPRConst.REPOSITORY_CLOUD_PUSH.equals(bitbucketEvent.getAction()),
            triggerFilter.getActionFilter() instanceof BitBucketPPRServerRepositoryPushActionFilter,
            BitBucketPPRConst.REPOSITORY_SERVER_PUSH, bitbucketEvent.getAction(),
            BitBucketPPRConst.REPOSITORY_SERVER_PUSH.equals(bitbucketEvent.getAction())});

    return ((triggerFilter.getActionFilter() instanceof BitBucketPPRRepositoryPushActionFilter
        && BitBucketPPRConst.REPOSITORY_CLOUD_PUSH.equals(bitbucketEvent.getAction()))
        || (triggerFilter.getActionFilter() instanceof BitBucketPPRServerRepositoryPushActionFilter
            && BitBucketPPRConst.REPOSITORY_SERVER_PUSH.equals(bitbucketEvent.getAction())));
  }
}
