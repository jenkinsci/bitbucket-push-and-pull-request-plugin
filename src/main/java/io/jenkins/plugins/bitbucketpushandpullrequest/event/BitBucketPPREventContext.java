package io.jenkins.plugins.bitbucketpushandpullrequest.event;

import hudson.model.Run;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;


public class BitBucketPPREventContext {
  private SCM scmTrigger;
  private Run<?, ?> run;
  private BitBucketPPRAction action;

  public BitBucketPPREventContext(BitBucketPPRAction action, SCM scmTrigger, Run<?, ?> run) {
    this.action = action;
    this.scmTrigger = scmTrigger;
    this.run = run;
  }

  public SCM getScmTrigger() {
    return scmTrigger;
  }

  public BitBucketPPRAction getAction() {
    return action;
  }

  public Run<?, ?> getRun() {
    return run;
  }
}
