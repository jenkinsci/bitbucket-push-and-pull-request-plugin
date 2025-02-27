/*******************************************************************************
 * The MIT License
 * <p>
 * Copyright (C) 2020, CloudBees, Inc.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.allowedBranches
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/


package io.jenkins.plugins.bitbucketpushandpullrequest.extensions.dsl;

import java.util.ArrayList;
import java.util.List;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCommentCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCommentDeletedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCommentUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestDeclinedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestMergedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerCommentCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerDeclinedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerMergedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerSourceUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRServerRepositoryPushActionFilter;
import javaposse.jobdsl.dsl.Context;

public class BitBucketPPRHookJobDslContext implements Context {
  List<BitBucketPPRTriggerFilter> triggers = new ArrayList<>();

  public void repositoryPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches) {
    BitBucketPPRRepositoryPushActionFilter repositoryPushActionFilter =
        new BitBucketPPRRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    BitBucketPPRRepositoryTriggerFilter repositoryTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryPushActionFilter);
    triggers.add(repositoryTriggerFilter);
  }

  public void repositoryPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches, boolean isToApprove) {
    BitBucketPPRRepositoryPushActionFilter repositoryPushActionFilter =
        new BitBucketPPRRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    repositoryPushActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRRepositoryTriggerFilter repositoryTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryPushActionFilter);
    triggers.add(repositoryTriggerFilter);
  }

  public void repositoryPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches, boolean isToApprove,
      boolean triggerOnlyIfTagPush) {
    BitBucketPPRRepositoryPushActionFilter repositoryPushActionFilter =
        new BitBucketPPRRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    repositoryPushActionFilter.setIsToApprove(isToApprove);
    repositoryPushActionFilter.setTriggerOnlyIfTagPush(triggerOnlyIfTagPush);
    BitBucketPPRRepositoryTriggerFilter repositoryTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryPushActionFilter);
    triggers.add(repositoryTriggerFilter);
  }

  public void pullRequestApprovedAction(boolean onlyIfReviewersApproved, String allowedBranches) {
    BitBucketPPRPullRequestApprovedActionFilter pullRequestApprovedActionFilter =
        new BitBucketPPRPullRequestApprovedActionFilter(onlyIfReviewersApproved);
    pullRequestApprovedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestApprovedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestApprovedAction(boolean onlyIfReviewersApproved) {
    BitBucketPPRPullRequestApprovedActionFilter pullRequestApprovedActionFilter =
        new BitBucketPPRPullRequestApprovedActionFilter(onlyIfReviewersApproved);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestApprovedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  @Deprecated
  public void pullRequestApprovedAction(boolean onlyIfReviewersApproved, String allowedBranches,
      boolean isToApprove) {
    BitBucketPPRPullRequestApprovedActionFilter pullRequestApprovedActionFilter =
        new BitBucketPPRPullRequestApprovedActionFilter(onlyIfReviewersApproved);
    pullRequestApprovedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestApprovedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCreatedAction() {
    BitBucketPPRPullRequestCreatedActionFilter pullRequestCreatedActionFilter =
        new BitBucketPPRPullRequestCreatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCreatedAction(String allowedBranches) {
    BitBucketPPRPullRequestCreatedActionFilter pullRequestCreatedActionFilter =
        new BitBucketPPRPullRequestCreatedActionFilter();
    pullRequestCreatedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCreatedAction(String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestCreatedActionFilter pullRequestCreatedActionFilter =
        new BitBucketPPRPullRequestCreatedActionFilter();
    pullRequestCreatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestCreatedActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCreatedAction(String allowedBranches, boolean isToApprove,
      boolean isToDecline) {
    BitBucketPPRPullRequestCreatedActionFilter pullRequestCreatedActionFilter =
        new BitBucketPPRPullRequestCreatedActionFilter();
    pullRequestCreatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestCreatedActionFilter.setIsToApprove(isToApprove);
    pullRequestCreatedActionFilter.setIsToDecline(isToDecline);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestUpdatedAction() {
    BitBucketPPRPullRequestUpdatedActionFilter pullRequestUpdatedActionFilter =
        new BitBucketPPRPullRequestUpdatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestUpdatedAction(String allowedBranches) {
    BitBucketPPRPullRequestUpdatedActionFilter pullRequestUpdatedActionFilter =
        new BitBucketPPRPullRequestUpdatedActionFilter();
    pullRequestUpdatedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestUpdatedAction(String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestUpdatedActionFilter pullRequestUpdatedActionFilter =
        new BitBucketPPRPullRequestUpdatedActionFilter();
    pullRequestUpdatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestUpdatedActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestUpdatedAction(String allowedBranches, boolean isToApprove,
      boolean isToDecline) {
    BitBucketPPRPullRequestUpdatedActionFilter pullRequestUpdatedActionFilter =
        new BitBucketPPRPullRequestUpdatedActionFilter();
    pullRequestUpdatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestUpdatedActionFilter.setIsToApprove(isToApprove);
    pullRequestUpdatedActionFilter.setIsToDecline(isToDecline);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestMergedAction() {
    BitBucketPPRPullRequestMergedActionFilter pullRequestMergedActionFilter =
        new BitBucketPPRPullRequestMergedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestMergedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestMergedAction(String allowedBranches) {
    BitBucketPPRPullRequestMergedActionFilter pullRequestMergedActionFilter =
        new BitBucketPPRPullRequestMergedActionFilter();
    pullRequestMergedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestMergedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestMergedAction(String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestMergedActionFilter pullRequestMergedActionFilter =
        new BitBucketPPRPullRequestMergedActionFilter();
    pullRequestMergedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestMergedActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestMergedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestDeclinedAction() {
    BitBucketPPRPullRequestDeclinedActionFilter pullRequestDeclinedActionFilter =
        new BitBucketPPRPullRequestDeclinedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestDeclinedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestDeclinedAction(String allowedBranches) {
    BitBucketPPRPullRequestDeclinedActionFilter pullRequestDeclinedActionFilter =
        new BitBucketPPRPullRequestDeclinedActionFilter();
    pullRequestDeclinedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestDeclinedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentCreatedAction() {
    BitBucketPPRPullRequestCommentCreatedActionFilter pullRequestCommentCreatedActionFilter =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentCreatedAction(String allowedBranches) {
    BitBucketPPRPullRequestCommentCreatedActionFilter pullRequestCommentCreatedActionFilter =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    pullRequestCommentCreatedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentCreatedAction(String allowedBranches, String commentFilter) {
    BitBucketPPRPullRequestCommentCreatedActionFilter pullRequestCommentCreatedActionFilter =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    pullRequestCommentCreatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestCommentCreatedActionFilter.setCommentFilter(commentFilter);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentUpdatedAction() {
    BitBucketPPRPullRequestCommentUpdatedActionFilter pullRequestCommentUpdatedActionFilter =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentUpdatedAction(String allowedBranches) {
    BitBucketPPRPullRequestCommentUpdatedActionFilter pullRequestCommentUpdatedActionFilter =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    pullRequestCommentUpdatedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentUpdatedAction(String allowedBranches, String commentFilter) {
    BitBucketPPRPullRequestCommentUpdatedActionFilter pullRequestCommentUpdatedActionFilter =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    pullRequestCommentUpdatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestCommentUpdatedActionFilter.setCommentFilter(commentFilter);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentDeletedAction() {
    BitBucketPPRPullRequestCommentDeletedActionFilter pullRequestCommentDeletedActionFilter =
        new BitBucketPPRPullRequestCommentDeletedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentDeletedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void pullRequestCommentDeletedAction(String allowedBranches) {
    BitBucketPPRPullRequestCommentDeletedActionFilter pullRequestCommentDeletedActionFilter =
        new BitBucketPPRPullRequestCommentDeletedActionFilter();
    pullRequestCommentDeletedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCommentDeletedActionFilter);
    triggers.add(pullRequestTriggerFilter);
  }

  public void repositoryServerPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches) {
    BitBucketPPRServerRepositoryPushActionFilter repositoryServerPushActionFilter =
        new BitBucketPPRServerRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    BitBucketPPRRepositoryTriggerFilter repositoryServerTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryServerPushActionFilter);
    triggers.add(repositoryServerTriggerFilter);
  }

  public void repositoryServerPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches, boolean isToApprove) {
    BitBucketPPRServerRepositoryPushActionFilter repositoryServerPushActionFilter =
        new BitBucketPPRServerRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    repositoryServerPushActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRRepositoryTriggerFilter repositoryServerTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryServerPushActionFilter);
    triggers.add(repositoryServerTriggerFilter);
  }

  public void repositoryServerPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches, boolean isToApprove,
      boolean triggerOnlyIfTagPush) {
    BitBucketPPRServerRepositoryPushActionFilter repositoryServerPushActionFilter =
        new BitBucketPPRServerRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    repositoryServerPushActionFilter.setIsToApprove(isToApprove);
    repositoryServerPushActionFilter.setTriggerOnlyIfTagPush(triggerOnlyIfTagPush);
    BitBucketPPRRepositoryTriggerFilter repositoryServerTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryServerPushActionFilter);
    triggers.add(repositoryServerTriggerFilter);
  }

  public void pullRequestServerApprovedAction(boolean onlyIfReviewersApproved) {
    BitBucketPPRPullRequestServerApprovedActionFilter pullRequestServerApprovedActionFilter =
        new BitBucketPPRPullRequestServerApprovedActionFilter(onlyIfReviewersApproved);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerApprovedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerApprovedAction(boolean onlyIfReviewersApproved,
      String allowedBranches) {
    BitBucketPPRPullRequestServerApprovedActionFilter pullRequestServerApprovedActionFilter =
        new BitBucketPPRPullRequestServerApprovedActionFilter(onlyIfReviewersApproved);
    pullRequestServerApprovedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerApprovedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }


  @Deprecated
  public void pullRequestServerApprovedAction(boolean onlyIfReviewersApproved,
      String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestServerApprovedActionFilter pullRequestServerApprovedActionFilter =
        new BitBucketPPRPullRequestServerApprovedActionFilter(onlyIfReviewersApproved);
    pullRequestServerApprovedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerApprovedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerCreatedAction() {
    BitBucketPPRPullRequestServerCreatedActionFilter pullRequestServerCreatedActionFilter =
        new BitBucketPPRPullRequestServerCreatedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCreatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerCreatedAction(String allowedBranches) {
    BitBucketPPRPullRequestServerCreatedActionFilter pullRequestServerCreatedActionFilter =
        new BitBucketPPRPullRequestServerCreatedActionFilter();
    pullRequestServerCreatedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCreatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerCreatedAction(String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestServerCreatedActionFilter pullRequestServerCreatedActionFilter =
        new BitBucketPPRPullRequestServerCreatedActionFilter();
    pullRequestServerCreatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestServerCreatedActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCreatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerCreatedAction(String allowedBranches, boolean isToApprove, boolean isToDecline) {
    BitBucketPPRPullRequestServerCreatedActionFilter pullRequestServerCreatedActionFilter =
        new BitBucketPPRPullRequestServerCreatedActionFilter();
    pullRequestServerCreatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestServerCreatedActionFilter.setIsToApprove(isToApprove);
    pullRequestServerCreatedActionFilter.setIsToDecline(isToDecline);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCreatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerUpdatedAction() {
    BitBucketPPRPullRequestServerUpdatedActionFilter pullRequestUpdatedServerActionFilter =
        new BitBucketPPRPullRequestServerUpdatedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestUpdatedServerActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerUpdatedAction(String allowedBranches) {
    BitBucketPPRPullRequestServerUpdatedActionFilter pullRequestUpdatedServerActionFilter =
        new BitBucketPPRPullRequestServerUpdatedActionFilter();
    pullRequestUpdatedServerActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestUpdatedServerActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerUpdatedAction(String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestServerUpdatedActionFilter pullRequestUpdatedServerActionFilter =
        new BitBucketPPRPullRequestServerUpdatedActionFilter();
    pullRequestUpdatedServerActionFilter.setAllowedBranches(allowedBranches);
    pullRequestUpdatedServerActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestUpdatedServerActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerUpdatedAction(String allowedBranches, boolean isToApprove, boolean isToDecline) {
    BitBucketPPRPullRequestServerUpdatedActionFilter pullRequestUpdatedServerActionFilter =
        new BitBucketPPRPullRequestServerUpdatedActionFilter();
    pullRequestUpdatedServerActionFilter.setAllowedBranches(allowedBranches);
    pullRequestUpdatedServerActionFilter.setIsToApprove(isToApprove);
    pullRequestUpdatedServerActionFilter.setIsToApprove(isToDecline);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestUpdatedServerActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerSourceUpdatedAction() {
    BitBucketPPRPullRequestServerSourceUpdatedActionFilter pullRequestServerSourceUpdatedActionFilter =
        new BitBucketPPRPullRequestServerSourceUpdatedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerSourceUpdatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerSourceUpdatedAction(String allowedBranches) {
    BitBucketPPRPullRequestServerSourceUpdatedActionFilter pullRequestServerSourceUpdatedActionFilter =
        new BitBucketPPRPullRequestServerSourceUpdatedActionFilter();
    pullRequestServerSourceUpdatedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerSourceUpdatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerSourceUpdatedAction(String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestServerSourceUpdatedActionFilter pullRequestServerSourceUpdatedActionFilter =
        new BitBucketPPRPullRequestServerSourceUpdatedActionFilter();
    pullRequestServerSourceUpdatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestServerSourceUpdatedActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerSourceUpdatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerSourceUpdatedAction(String allowedBranches, boolean isToApprove, boolean isToDecline) {
    BitBucketPPRPullRequestServerSourceUpdatedActionFilter pullRequestServerSourceUpdatedActionFilter =
        new BitBucketPPRPullRequestServerSourceUpdatedActionFilter();
    pullRequestServerSourceUpdatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestServerSourceUpdatedActionFilter.setIsToApprove(isToApprove);
    pullRequestServerSourceUpdatedActionFilter.setIsToApprove(isToDecline);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerSourceUpdatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerMergedAction() {
    BitBucketPPRPullRequestServerMergedActionFilter pullRequestServerMegedActionFilter =
        new BitBucketPPRPullRequestServerMergedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerMegedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerMergedAction(String allowedBranches) {
    BitBucketPPRPullRequestServerMergedActionFilter pullRequestServerMergedActionFilter =
        new BitBucketPPRPullRequestServerMergedActionFilter();
    pullRequestServerMergedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerMergedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerMergedAction(String allowedBranches, boolean isToApprove) {
    BitBucketPPRPullRequestServerMergedActionFilter pullRequestServerMergedActionFilter =
        new BitBucketPPRPullRequestServerMergedActionFilter();
    pullRequestServerMergedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestServerMergedActionFilter.setIsToApprove(isToApprove);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerMergedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerDeclinedAction() {
    BitBucketPPRPullRequestServerDeclinedActionFilter pullRequestServerDeclinedActionFilter =
        new BitBucketPPRPullRequestServerDeclinedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerDeclinedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerDeclinedAction(String allowedBranches) {
    BitBucketPPRPullRequestServerDeclinedActionFilter pullRequestServerDeclinedActionFilter =
        new BitBucketPPRPullRequestServerDeclinedActionFilter();
    pullRequestServerDeclinedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerDeclinedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerCommentCreatedAction() {
    BitBucketPPRPullRequestServerCommentCreatedActionFilter pullRequestServerCommentCreatedActionFilter =
        new BitBucketPPRPullRequestServerCommentCreatedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCommentCreatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerCommentCreatedAction(String allowedBranches) {
    BitBucketPPRPullRequestServerCommentCreatedActionFilter pullRequestServerCommentCreatedActionFilter =
        new BitBucketPPRPullRequestServerCommentCreatedActionFilter();
    pullRequestServerCommentCreatedActionFilter.setAllowedBranches(allowedBranches);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCommentCreatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

  public void pullRequestServerCommentCreatedAction(String allowedBranches, String commentFilter) {
    BitBucketPPRPullRequestServerCommentCreatedActionFilter pullRequestServerCommentCreatedActionFilter =
        new BitBucketPPRPullRequestServerCommentCreatedActionFilter();
    pullRequestServerCommentCreatedActionFilter.setAllowedBranches(allowedBranches);
    pullRequestServerCommentCreatedActionFilter.setCommentFilter(commentFilter);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCommentCreatedActionFilter);
    triggers.add(pullRequestServerTriggerFilter);
  }

}
