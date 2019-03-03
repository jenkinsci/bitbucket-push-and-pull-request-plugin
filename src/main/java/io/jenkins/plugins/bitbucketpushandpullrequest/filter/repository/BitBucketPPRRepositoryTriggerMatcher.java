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


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPREventTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts;


public class BitBucketPPRRepositoryTriggerMatcher implements BitBucketPPREventTriggerMatcher {
  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPRRepositoryTriggerMatcher.class.getName());

  @Override
  public boolean matchesAction(BitBucketPPREvent bitbucketEvent,
      BitBucketPPRTriggerFilter triggerFilter) {
    LOGGER.log(Level.INFO, () -> "" + bitbucketEvent.toString());

    return (triggerFilter.getActionFilter() instanceof BitBucketPPRRepositoryPushActionFilter
        && (BitBucketPPRConsts.REPOSITORY_PUSH.equals(bitbucketEvent.getAction())
            || BitBucketPPRConsts.REPOSITORY_SERVER_PUSH.equals(bitbucketEvent.getAction())));
  }
}
