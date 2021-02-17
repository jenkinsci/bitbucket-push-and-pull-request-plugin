package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventType;

public abstract class BitBucketPPRHandlerTemplate {

  public void run(BitBucketPPREventType eventType) throws Exception {
    BitBucketPPRPluginConfig config = BitBucketPPRPluginConfig.getInstance();

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
}
