package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventType;

public abstract class BitBucketPPRHandlerTemplate {

  public void run(BitBucketPPREventType eventType) throws Exception {
    switch (eventType) {
      case BUILD_STARTED:
        setBuildStatusInProgress();
        break;
      case BUILD_FINISHED:
        setBuildStatusOnFinished();
        setApproved();
        break;
      default:
        throw new Exception();
    }
  }


  protected abstract void setApproved();

  protected abstract void setBuildStatusOnFinished();

  protected abstract void setBuildStatusInProgress();
}
