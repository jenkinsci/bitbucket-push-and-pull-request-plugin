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

import java.io.File;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.server.BitBucketPPRPullRequestServerUpdatedCause;


public class BitBucketPPRPullRequestServerMergedActionFilter
    extends BitBucketPPRPullRequestServerActionFilter {

  public String allowedBranches;

  @DataBoundConstructor
  public BitBucketPPRPullRequestServerMergedActionFilter() {}

  @DataBoundSetter
  public void setAllowedBranches(String allowedBranches) {
    if (allowedBranches == null) {
      this.allowedBranches = "";
    } else {
      this.allowedBranches = allowedBranches;
    }
  }

  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    return matches(allowedBranches, bitbucketAction.getTargetBranch(), null);
  }

  @Override
  public BitBucketPPRTriggerCause getCause(File pollingLog, BitBucketPPRAction pullRequestAction)
      throws IOException {
    return new BitBucketPPRPullRequestServerUpdatedCause(pollingLog, pullRequestAction);
  }

  @Extension
  public static class ActionFilterDescriptorImpl
      extends BitBucketPPRPullRequestServerActionDescriptor {

    @Override
    public String getDisplayName() {
      return "Merged";
    }
  }
}
