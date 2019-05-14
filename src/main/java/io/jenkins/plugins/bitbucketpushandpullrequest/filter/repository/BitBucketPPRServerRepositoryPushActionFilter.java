package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.plugins.git.BranchSpec;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;


public class BitBucketPPRServerRepositoryPushActionFilter extends BitBucketPPRRepositoryActionFilter {
  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPRServerRepositoryPushActionFilter.class.getName());

  public boolean triggerAlsoIfTagPush;
  public String allowedBranches;

@DataBoundConstructor
public BitBucketPPRServerRepositoryPushActionFilter(boolean triggerAlsoIfTagPush,
    String allowedBranches) {
  this.triggerAlsoIfTagPush = triggerAlsoIfTagPush;
  this.allowedBranches = allowedBranches;
}

  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    if (bitbucketAction.getType().equalsIgnoreCase("BRANCH")
        || bitbucketAction.getType().equalsIgnoreCase("UPDATE") || this.triggerAlsoIfTagPush) {
      return matches(new BranchSpec(bitbucketAction.getBranchName()), this.allowedBranches);
    }
    return false;
  }

  protected boolean matches(BranchSpec branchSpec, String allowedBranches) {
    LOGGER.info("Should trigger build?");
    if (this.allowedBranches.isEmpty()) {
      LOGGER.info("allowed branches are not set.");
      return true;
    }

    LOGGER.info("Allowed branches are set.");
    String[] branches = this.allowedBranches.split(",");
    Arrays.stream(branches).map(String::trim).toArray(unused -> branches);

    for (String branchPattern : branches) {
      if (branchSpec.matches(branchPattern)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public BitBucketPPRTriggerCause getCause(File pollingLog, BitBucketPPRAction bitbucketAction)
      throws IOException {
    return new BitBucketPPRServerRepositoryCause(pollingLog, bitbucketAction);
  }

  @Extension
  public static class ActionFilterDescriptorImpl extends BitBucketPPRRepositoryActionDescriptor {

    @Override
    public String getDisplayName() {
      return "Bitbucket Server Push";
    }
  }
}
