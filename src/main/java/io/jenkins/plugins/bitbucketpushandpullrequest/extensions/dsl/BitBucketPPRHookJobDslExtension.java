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


package io.jenkins.plugins.bitbucketpushandpullrequest.extensions.dsl;

import java.util.ArrayList;
import java.util.List;
import hudson.Extension;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestMergedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerMergedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server.BitBucketPPRPullRequestServerUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRServerRepositoryPushActionFilter;
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import javaposse.jobdsl.plugin.DslEnvironment;
import io.jenkins.plugins.bitbucketpushandpullrequest.extensions.dsl.BitBucketPPRHookJobDslContext;

@Extension(optional = true)
public class BitBucketPPRHookJobDslExtension extends ContextExtensionPoint {

  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketTriggers(Runnable closure) {
    BitBucketPPRHookJobDslContext context = new BitBucketPPRHookJobDslContext();
    executeInContext(closure, context);

    return new BitBucketPPRTrigger(context.triggers);
  }

  // @Deprecated
  // @DslExtensionMethod(context = TriggerContext.class)
  // public Object bitbucketPush() {
  // return bitbucketRepositoryPushAction(false, null);
  // }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketRepositoryPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches) {
    List<BitBucketPPRTriggerFilter> triggers = new ArrayList<>();
    BitBucketPPRRepositoryPushActionFilter repositoryPushActionFilter =
        new BitBucketPPRRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    BitBucketPPRRepositoryTriggerFilter repositoryTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryPushActionFilter);
    triggers.add(repositoryTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestApprovedAction(boolean onlyIfReviewersApproved) {
    List<BitBucketPPRTriggerFilter> triggers = new ArrayList<>();
    BitBucketPPRPullRequestApprovedActionFilter pullRequestApprovedActionFilter =
        new BitBucketPPRPullRequestApprovedActionFilter(onlyIfReviewersApproved);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestApprovedActionFilter);
    triggers.add(pullRequestTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestCreatedAction() {
    List<BitBucketPPRTriggerFilter> triggers = new ArrayList<>();
    BitBucketPPRPullRequestCreatedActionFilter pullRequestCreatedActionFilter =
        new BitBucketPPRPullRequestCreatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCreatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestUpdatedAction() {
    List<BitBucketPPRTriggerFilter> triggers = new ArrayList<>();
    BitBucketPPRPullRequestUpdatedActionFilter pullRequestUpdatedActionFilter =
        new BitBucketPPRPullRequestUpdatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestUpdatedActionFilter);
    triggers.add(pullRequestTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestMergedAction() {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestMergedActionFilter pullRequestMegedActionFilter =
        new BitBucketPPRPullRequestMergedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestMegedActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketRepositoryServerPushAction(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches) {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRServerRepositoryPushActionFilter repositoryServerPushActionFilter =
        new BitBucketPPRServerRepositoryPushActionFilter(triggerAlsoIfTagPush,
            triggerAlsoIfNothingChanged, allowedBranches);
    BitBucketPPRRepositoryTriggerFilter repositoryServerTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryServerPushActionFilter);
    triggers = new ArrayList<>();
    triggers.add(repositoryServerTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestServerApprovedAction(boolean onlyIfReviewersApproved) {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestServerApprovedActionFilter pullRequestServerApprovedActionFilter =
        new BitBucketPPRPullRequestServerApprovedActionFilter(onlyIfReviewersApproved);
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerApprovedActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestServerTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestServerCreatedAction() {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestServerCreatedActionFilter pullRequestServerCreatedActionFilter =
        new BitBucketPPRPullRequestServerCreatedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerCreatedActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestServerTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestServerUpdatedAction() {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestServerUpdatedActionFilter pullRequestUpdatedServerActionFilter =
        new BitBucketPPRPullRequestServerUpdatedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestUpdatedServerActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestServerTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestServerMergedAction() {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestServerMergedActionFilter pullRequestServerMegedActionFilter =
        new BitBucketPPRPullRequestServerMergedActionFilter();
    BitBucketPPRPullRequestServerTriggerFilter pullRequestServerTriggerFilter =
        new BitBucketPPRPullRequestServerTriggerFilter(pullRequestServerMegedActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestServerTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }
}
