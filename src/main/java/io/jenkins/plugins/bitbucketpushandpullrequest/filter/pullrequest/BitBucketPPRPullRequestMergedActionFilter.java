package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest;

import java.io.File;
import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.BitBucketPPRPullRequestUpdatedCause;


public class BitBucketPPRPullRequestMergedActionFilter extends BitBucketPPRPullRequestActionFilter {

  @DataBoundConstructor
  public BitBucketPPRPullRequestMergedActionFilter() {}

  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    return true;
  }

  @Override
  public BitBucketPPRTriggerCause getCause(File pollingLog, BitBucketPPRAction pullRequestAction)
      throws IOException {
    return new BitBucketPPRPullRequestUpdatedCause(pollingLog, pullRequestAction);
  }

  @Extension
    public static class ActionFilterDescriptorImpl extends BitBucketPPRPullRequestActionDescriptor {

      @Override
      public String getDisplayName() {
        return "Merged";
      }
    }
}