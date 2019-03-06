package io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository;

import java.io.File;
import java.io.IOException;

import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;

public class BitBucketPPRServerRepositoryCause extends BitBucketPPRTriggerCause {
  
  public BitBucketPPRServerRepositoryCause(File pollingLog, BitBucketPPRAction bitbucketAction)
      throws IOException {
    super(pollingLog, bitbucketAction);
  }
  
  public BitBucketPPRServerRepositoryAction getServerRepositoryPayLoad() {
    return (BitBucketPPRServerRepositoryAction) super.getAction();
  }
  
  @Override
  public String getShortDescription() {
    String pusher = bitbucketAction.getUser() != null ? bitbucketAction.getUser() : "";
    return "Started by Bitbucket repository event by " + pusher;
  }
}
