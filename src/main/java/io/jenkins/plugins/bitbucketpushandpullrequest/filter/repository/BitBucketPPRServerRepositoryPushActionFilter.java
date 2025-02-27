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
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import static java.util.Objects.isNull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;


public class BitBucketPPRServerRepositoryPushActionFilter
    extends BitBucketPPRRepositoryActionFilter {

  private static final Logger logger =
      Logger.getLogger(BitBucketPPRServerRepositoryPushActionFilter.class.getName());

  public boolean triggerAlsoIfTagPush;
  public boolean triggerOnlyIfTagPush;
  public boolean triggerAlsoIfNothingChanged;
  public String allowedBranches;
  public boolean isToApprove;

  @DataBoundConstructor
  public BitBucketPPRServerRepositoryPushActionFilter(boolean triggerAlsoIfTagPush,
      boolean triggerAlsoIfNothingChanged, String allowedBranches) {
    this.triggerAlsoIfTagPush = triggerAlsoIfTagPush;
    this.triggerAlsoIfNothingChanged = triggerAlsoIfNothingChanged;
    this.allowedBranches = allowedBranches;
  }

  @DataBoundSetter
  public void setTriggerOnlyIfTagPush(boolean triggerOnlyIfTagPush) {
    this.triggerOnlyIfTagPush = triggerOnlyIfTagPush;
  }

  @DataBoundSetter
  public void setIsToApprove(boolean isToApprove) {
    this.isToApprove = isToApprove;
  }

  @Override
  public boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction) {
    logger
        .info(() -> "Should trigger build for bitbucket action" + bitbucketAction.toString() + "?");

    if (isNull(bitbucketAction) || isNull(bitbucketAction.getType())) {
      logger.info(
          "The bitbucketAction or the bitbucketAction type are not set.");
      return false;
    }

    if (!bitbucketAction.getType().equalsIgnoreCase("BRANCH")
        && !bitbucketAction.getType().equalsIgnoreCase("named_branch")
        && !bitbucketAction.getType().equalsIgnoreCase("UPDATE")
        && !bitbucketAction.getType().equalsIgnoreCase("TAG")
        && !this.triggerAlsoIfTagPush) {
      logger.info(
          () ->
              "Neither bitbucketActionType is BRANCH, nor UPDATE, nor trigger on tag push is set for bitbucket type: "
                  + bitbucketAction.getType() + ".");

      return false;
    }

    if (this.triggerOnlyIfTagPush && !bitbucketAction.getType().equalsIgnoreCase("TAG")) {
      return false;
    }

    logger.log(Level.FINEST, "the target branch is: {0}.", bitbucketAction.getTargetBranch());
    logger.log(Level.FINEST, "the target branch ref id is: {0}.",
        bitbucketAction.getTargetBranchRefId());

    logger.log(Level.FINEST, "The allowed branches are: {0}.", allowedBranches);
    return matches(allowedBranches, bitbucketAction.getTargetBranch(), null)
        || matches(allowedBranches, bitbucketAction.getTargetBranchRefId(), null);
  }

  @Override
  public BitBucketPPRTriggerCause getCause(File pollingLog, BitBucketPPRAction bitbucketAction,
      BitBucketPPRHookEvent bitBucketEvent)
      throws IOException {
    return new BitBucketPPRServerRepositoryCause(pollingLog, bitbucketAction, bitBucketEvent);
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

  @Override
  public boolean shouldSendApprove() {
    return isToApprove;
  }

  @Extension
  public static class ActionFilterDescriptorImpl extends BitBucketPPRRepositoryActionDescriptor {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Bitbucket Server Push";
    }
  }
}
