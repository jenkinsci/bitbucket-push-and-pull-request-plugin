package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import hudson.model.Job;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventType;

public abstract class BitBucketPPRHandlerTemplate {

  public void run(BitBucketPPREventType eventType) throws Exception {
    BitBucketPPRPluginConfig config = getGlobalConfig();
    switch (eventType) {
      case BUILD_STARTED:
        if (config.shouldNotifyBitBucket()) {
          setBuildStatusInProgress();
        }
        break;
      case BUILD_FINISHED:
        if (config.shouldNotifyBitBucket()) {
          setBuildStatusOnFinished();
          setApproved();
        }
        break;
      default:
        throw new Exception();
    }
  }

  public void setApproved() {
    return;
  }

  public abstract void setBuildStatusOnFinished();

  public abstract void setBuildStatusInProgress();

  protected BitBucketPPRPluginConfig getGlobalConfig(){
    return BitBucketPPRPluginConfig.getInstance();
  }

  protected String computeBitBucketBuildKey(BitBucketPPREventContext context) {
    if (getGlobalConfig().shouldUseJobNameAsBuildKey()) {
      Job<?, ?> job;
      if (context.getRun() == null) {
        job = context.getJob();
      } else {
        job = context.getRun().getParent();
      }
      return job.getDisplayName();
    } else {
      int buildNumber;
      if (context.getRun() == null) {
        buildNumber = context.getJobNextBuildNumber();
      } else {
        buildNumber = context.getRun().getNumber();
      }
      return Integer.toString(buildNumber);
    }
  }
}
