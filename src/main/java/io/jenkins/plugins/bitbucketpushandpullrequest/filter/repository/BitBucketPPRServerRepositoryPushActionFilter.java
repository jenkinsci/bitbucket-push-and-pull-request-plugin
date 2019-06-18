package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import java.io.File;
import java.io.IOException;
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

  public final boolean triggerAlsoIfTagPush;
  public final String allowedBranches;

@DataBoundConstructor
public BitBucketPPRServerRepositoryPushActionFilter(boolean triggerAlsoIfTagPush,
    String allowedBranches) {
  this.triggerAlsoIfTagPush = triggerAlsoIfTagPush;
  this.allowedBranches = allowedBranches;
}

  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    LOGGER.info("Should trigger build?");
    
    if (! bitbucketAction.getType().equalsIgnoreCase("BRANCH")
        && ! bitbucketAction.getType().equalsIgnoreCase("UPDATE") 
        && ! this.triggerAlsoIfTagPush) {
      LOGGER.info("Neither bitbucketActionType is BRANCH, nor UPDATE, nor trigger on tag push is set.");
      
      return false;
    }
    
    return matches(bitbucketAction.getBranchName(), this.allowedBranches);
  }

  protected boolean matches(String branchName, String allowedBranches) {
    LOGGER.info("Should trigger build?");
    if (this.allowedBranches.isEmpty()) {
      LOGGER.info("allowed branches are not set.");
      return true;
    }

    LOGGER.info(() -> "Following allowed branches patterns are set: " + allowedBranches);    
    LOGGER.info(() -> "The branchName in action is: " + branchName);
    

    String[] branchSpecs = allowedBranches.split(",");
    for (String branchSpec: branchSpecs) {
      LOGGER.info(() -> "Matching branch: " + branchName + " with branchSpec: " + branchSpec);
      if (new BranchSpec(branchSpec.trim()).matches(branchName)) {
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
