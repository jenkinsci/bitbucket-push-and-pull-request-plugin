/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2019, CloudBees, Inc.
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

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.PULL_REQUEST_REVIEWER;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.server.BitBucketPPRPullRequestServerApprovedCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRParticipant;

public class BitBucketPPRPullRequestServerApprovedActionFilter
    extends BitBucketPPRPullRequestServerActionFilter {
  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPRPullRequestApprovedActionFilter.class.getName());

  public String allowedBranches;
  public boolean triggerOnlyIfAllReviewersApproved;
  public boolean isToApprove;

  @DataBoundConstructor
  public BitBucketPPRPullRequestServerApprovedActionFilter(
      boolean triggerOnlyIfAllReviewersApproved) {
    this.triggerOnlyIfAllReviewersApproved = triggerOnlyIfAllReviewersApproved;
  }

  @DataBoundSetter
  public void setAllowedBranches(String allowedBranches) {
    if (allowedBranches == null) {
      this.allowedBranches = "";
    } else {
      this.allowedBranches = allowedBranches;
    }
  }

  @DataBoundSetter
  public void setIsToApprove(boolean isToApprove) {
    this.isToApprove = isToApprove;
  }

  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    if (triggerOnlyIfAllReviewersApproved && !allReviewersHaveApproved(bitbucketAction)) {
      LOGGER.info("Not triggered because not all reviewers have approved the pull request");
      return false;
    }

    return matches(allowedBranches, bitbucketAction.getTargetBranch(), null);
  }

  @Override
  public BitBucketPPRTriggerCause getCause(File pollingLog, BitBucketPPRAction pullRequestAction, 
      BitBucketPPRHookEvent bitBucketEvent)
      throws IOException {
    return new BitBucketPPRPullRequestServerApprovedCause(pollingLog, pullRequestAction, bitBucketEvent);
  }

  @Extension
  public static class ActionFilterDescriptorImpl
      extends BitBucketPPRPullRequestServerActionDescriptor {

    @Override
    public String getDisplayName() {
      return "Approved";
    }
  }

  public boolean getTriggerOnlyIfAllReviewersApproved() {
    return triggerOnlyIfAllReviewersApproved;
  }

  private boolean allReviewersHaveApproved(BitBucketPPRAction pullRequestAction) {
    return pullRequestAction.getPayload().getPullRequest().getParticipants().stream()
        .filter(p -> isReviewer(p) && !p.getApproved()).count() == 0;
  }

  private boolean isReviewer(BitBucketPPRParticipant pullRequestParticipant) {
    String role = pullRequestParticipant.getRole();

    return PULL_REQUEST_REVIEWER.equals(role);
  }

  @Override
  public boolean shouldSendApprove() {
    return isToApprove;
  }
}
