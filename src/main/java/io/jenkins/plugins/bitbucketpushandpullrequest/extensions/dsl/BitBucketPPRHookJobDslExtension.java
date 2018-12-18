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
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.BitBucketPPRPullRequestApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.BitBucketPPRPullRequestCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.BitBucketPPRPullRequestTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.BitBucketPPRPullRequestUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryTriggerFilter;
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;


@Extension(optional = true)
public class BitBucketPPRHookJobDslExtension extends ContextExtensionPoint {

  @Deprecated
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPush() {
    return bitbucketRepositoryPushAction(false, null);
  }

  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketRepositoryPushAction(boolean triggerAlsoIfTagPush,
      String allowedBranches) {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRRepositoryPushActionFilter repositoryPushActionFilter =
        new BitBucketPPRRepositoryPushActionFilter(triggerAlsoIfTagPush, allowedBranches);
    BitBucketPPRRepositoryTriggerFilter repositoryTriggerFilter =
        new BitBucketPPRRepositoryTriggerFilter(repositoryPushActionFilter);
    triggers = new ArrayList<>();
    triggers.add(repositoryTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestApprovedAction(boolean onlyIfReviewersApproved) {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestApprovedActionFilter pullRequestApprovedActionFilter =
        new BitBucketPPRPullRequestApprovedActionFilter(onlyIfReviewersApproved);
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestApprovedActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestCreatedAction() {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestCreatedActionFilter pullRequestCreatedActionFilter =
        new BitBucketPPRPullRequestCreatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestCreatedActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }

  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketPullRequestUpdatedAction() {
    List<BitBucketPPRTriggerFilter> triggers;
    BitBucketPPRPullRequestUpdatedActionFilter pullRequestUpdatedActionFilter =
        new BitBucketPPRPullRequestUpdatedActionFilter();
    BitBucketPPRPullRequestTriggerFilter pullRequestTriggerFilter =
        new BitBucketPPRPullRequestTriggerFilter(pullRequestUpdatedActionFilter);
    triggers = new ArrayList<>();
    triggers.add(pullRequestTriggerFilter);
    return new BitBucketPPRTrigger(triggers);
  }
}
