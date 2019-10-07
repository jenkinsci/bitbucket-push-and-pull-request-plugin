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
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRRepositoryCause;


public class BitBucketPPRRepositoryPushActionFilter extends BitBucketPPRRepositoryActionFilter {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRRepositoryPushActionFilter.class.getName());

  public boolean triggerAlsoIfTagPush;
  public boolean triggerAlsoIfNothingChanged;
  public String allowedBranches;

  @DataBoundConstructor
  public BitBucketPPRRepositoryPushActionFilter(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches) {
    this.triggerAlsoIfTagPush = triggerAlsoIfTagPush;
    this.triggerAlsoIfNothingChanged = triggerAlsoIfNothingChanged;
    this.allowedBranches = allowedBranches;
  }


  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    logger.info(
        () -> "Should trigger build for the bitbucket action: " + bitbucketAction.toString() + "?");

    if (!bitbucketAction.getType().equalsIgnoreCase("BRANCH")
        && !bitbucketAction.getType().equalsIgnoreCase("named_branch")
        && !bitbucketAction.getType().equalsIgnoreCase("UPDATE") && !this.triggerAlsoIfTagPush) {
      logger.info(
          "Neither bitbucketAction type is BRANCH, nor UPDATE, nor trigger on tag push is set: "
              + bitbucketAction.getType());

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
    return new BitBucketPPRRepositoryCause(pollingLog, bitbucketAction);
  }

  public String getAllowedBranches() {
    return allowedBranches;
  }

  public void setAllowedBranches(String allowedBranches) {
    this.allowedBranches = allowedBranches;
  }
  
  @Override
  public boolean shouldTriggerAlsoIfNothingChanged() {
    return triggerAlsoIfNothingChanged;
  }

  @Extension
  public static class ActionFilterDescriptorImpl extends BitBucketPPRRepositoryActionDescriptor {

    @Override
    public String getDisplayName() {
      return "Bitbucket Cloud Push";
    }
  }
}
