package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
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

  public UserRemoteConfig getUserRemoteConfigs(SCM scm) {
    GitSCM gitSCM = (GitSCM) scm;
    UserRemoteConfig config = gitSCM.getUserRemoteConfigs().get(0);
    return config;
  }

  public void setApproved() {
    return;
  }

  public abstract void setBuildStatusOnFinished();

  public abstract void setBuildStatusInProgress();
}
