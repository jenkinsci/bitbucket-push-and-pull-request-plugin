package io.jenkins.plugins.bitbucketpushandpullrequest.event;

import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import hudson.model.queue.QueueTaskFuture;
import hudson.scm.SCM;


public class BitBucketPPREventContext {
  private SCM scmTrigger;
  private QueueTaskFuture<?> future;
  private BitBucketPPRAction action;

  public BitBucketPPREventContext(BitBucketPPRAction action, SCM scmTrigger, QueueTaskFuture<?> future) {
    this.action = action;
    this.scmTrigger = scmTrigger;
    this.future = future;
  }

  public SCM getScmTrigger() {
    return scmTrigger;
  }

  public BitBucketPPRAction getAction() {
    return action;
  }

  public QueueTaskFuture<?> getFuture() {
    return future;
  }
}
