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

package io.jenkins.plugins.bitbucketpushandpullrequest.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryTriggerMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;

public class BitBucketPPRFilterMatcher {
  private static final Logger logger = Logger.getLogger(BitBucketPPRFilterMatcher.class.getName());

  public List<BitBucketPPRTriggerFilter> getMatchingFilters(BitBucketPPRHookEvent event,
      List<BitBucketPPRTriggerFilter> triggerFilterList) {
    List<BitBucketPPRTriggerFilter> filteredList = new ArrayList<>();

    if (triggerFilterList != null) {
      for (BitBucketPPRTriggerFilter triggerFilter : triggerFilterList) {
        if (matchesEventAndAction(event, triggerFilter)) {
          filteredList.add(triggerFilter);
          logger.log(Level.INFO, "Event {0} matches trigger filter {1}",
              new Object[] {event, triggerFilter});
        } else {
          logger.log(Level.INFO, "Event {0} doesnt match trigger filter {1}",
              new Object[] {event, triggerFilter});
        }

      }
    }

    return filteredList;
  }

  private boolean matchesEventAndAction(BitBucketPPRHookEvent event,
      BitBucketPPRTriggerFilter triggerFilter) {
    if (BitBucketPPRConst.PULL_REQUEST_CLOUD_EVENT.equalsIgnoreCase(event.getEvent())
        && triggerFilter instanceof BitBucketPPRPullRequestTriggerFilter) {
      return new BitBucketPPRPullRequestTriggerMatcher().matchesAction(event, triggerFilter);
    } else if (BitBucketPPRConst.PULL_REQUEST_SERVER_EVENT.equalsIgnoreCase(event.getEvent())
        && triggerFilter instanceof BitBucketPPRPullRequestServerTriggerFilter) {
      return new BitBucketPPRPullRequestServerTriggerMatcher().matchesAction(event, triggerFilter);
    } else if (BitBucketPPRConst.REPOSITORY_EVENT.equalsIgnoreCase(event.getEvent())
        && triggerFilter instanceof BitBucketPPRRepositoryTriggerFilter) {
      return new BitBucketPPRRepositoryTriggerMatcher().matchesAction(event, triggerFilter);
    }

    return false;
  }
}
