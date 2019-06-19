package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.plugins.git.BranchSpec;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;


public class BitBucketPPRServerRepositoryPushActionFilter
    extends BitBucketPPRRepositoryActionFilter {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRServerRepositoryPushActionFilter.class.getName());

  public boolean triggerAlsoIfTagPush;
  private String allowedBranches;

  @DataBoundConstructor
  public BitBucketPPRServerRepositoryPushActionFilter(boolean triggerAlsoIfTagPush,
      String allowedBranches) {
    this.triggerAlsoIfTagPush = triggerAlsoIfTagPush;
    this.allowedBranches = allowedBranches;
  }

  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    logger
        .info(() -> "Should trigger build for bitbucket action" + bitbucketAction.toString() + "?");

    if (!bitbucketAction.getType().equalsIgnoreCase("BRANCH")
        && !bitbucketAction.getType().equalsIgnoreCase("named_branch")
        && !bitbucketAction.getType().equalsIgnoreCase("UPDATE") && !this.triggerAlsoIfTagPush) {
      logger.info(
          () -> "Neither bitbucketActionType is BRANCH, nor UPDATE, nor trigger on tag push is set for bitbucket type: "
              + bitbucketAction.getType() + ".");

      return false;
    }

    return matches(bitbucketAction.getBranchName());
  }

  protected boolean matches(String branchName) {
    logger.info(() -> "Following allowed branches patterns are set: " + allowedBranches);
    logger.info(() -> "The branchName in action is: " + branchName);

    String[] branchSpecs = allowedBranches.split(",");
    for (String branchSpec : branchSpecs) {
      BranchSpec pattern = new BranchSpec(branchSpec.trim());

      logger.info(() -> "Matching branch: " + branchName + " with branchSpec pattern: "
          + pattern.getName());

      if (pattern.matches(branchName)) {
        return true;
      }
    }

    return false;
  }

  public boolean matches(String branchName, EnvVars env) {
    logger.info(() -> "Following allowed branches patterns are set: " + allowedBranches);
    logger.info(() -> "The branchName in action is: " + branchName);

    String[] branchSpecs = allowedBranches.split(",");

    for (String branchSpec : branchSpecs) {
      BranchSpec pattern = new BranchSpec(branchSpec.trim());

      logger.info(() -> "Matching branch: " + branchName + " with branchSpec pattern: "
          + pattern.getName());

      if (pattern.matches(branchName, env)) {
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

  public String getAllowedBranches() {
    return allowedBranches;
  }

  public void setAllowedBranches(String allowedBranches) {
    this.allowedBranches = allowedBranches;
  }

  @Extension
  public static class ActionFilterDescriptorImpl extends BitBucketPPRRepositoryActionDescriptor {

    @Override
    public String getDisplayName() {
      return "Bitbucket Server Push";
    }
  }
}
