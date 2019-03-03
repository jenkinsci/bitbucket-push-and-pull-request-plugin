package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRServerPayload implements BitBucketPPRPayload {
  private BitBucketPPRServerActor actor;
  private BitBucketPPRServerRepository repository;
  private BitBucketPPRServerChange[] changes;

  @Override
  public BitBucketPPRServerActor getServerActor() {
    return actor;
  }


  @Override
  public BitBucketPPRServerRepository getServerRepository() {
    return repository;
  }


  @Override
  public BitBucketPPRServerChange[] getServerChanges() {
    return changes;
  }
}
