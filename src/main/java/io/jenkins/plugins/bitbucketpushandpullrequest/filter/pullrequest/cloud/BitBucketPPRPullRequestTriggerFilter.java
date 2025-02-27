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

package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilterDescriptor;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import jenkins.model.Jenkins;

public class BitBucketPPRPullRequestTriggerFilter extends BitBucketPPRTriggerFilter {
  public BitBucketPPRPullRequestActionFilter actionFilter;

  @DataBoundConstructor
  public BitBucketPPRPullRequestTriggerFilter(BitBucketPPRPullRequestActionFilter actionFilter) {
    this.actionFilter = actionFilter;
  }

  @Override
  public boolean shouldScheduleJob(BitBucketPPRAction bitbucketAction) {
    return actionFilter.shouldTriggerBuild(bitbucketAction);
  }

  @Override
  public boolean shouldSendApprove() {
    return actionFilter.shouldSendApprove();
  }

  @Override
  public boolean shouldSendDecline() {
    return actionFilter.shouldSendDecline();
  }

  @Override
  public BitBucketPPRTriggerCause getCause(
      File pollingLog, BitBucketPPRAction action, BitBucketPPRHookEvent bitBucketEvent)
      throws IOException {
    return actionFilter.getCause(pollingLog, action, bitBucketEvent);
  }

  @Symbol("bitbucketCloudPullRequest")
  @Extension
  public static class FilterDescriptorImpl extends BitBucketPPRTriggerFilterDescriptor {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Bitbucket Cloud Pull Request";
    }

    public List<BitBucketPPRPullRequestActionDescriptor> getActionDescriptors() {
      // you may want to filter this list of descriptors here, if you are being very fancy
      return Jenkins.get().getDescriptorList(BitBucketPPRPullRequestActionFilter.class);
    }
  }

  public AbstractDescribableImpl<?> getActionFilter() {
    return actionFilter;
  }

  @Override
  public String toString() {
    return "BitBucketPPRPullRequestTriggerFilter [actionFilter=" + actionFilter + "]";
  }

  @Override
  public boolean shouldTriggerAlsoIfNothingChanged() {
    return true;
  }
}
