package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRServerPayload implements BitBucketPPRPayload {
  private BitBucketPPRServerActor actor;
  private BitBucketPPRServerPullRequest pullRequest;
  private List<BitBucketPPRServerChange> changes = new ArrayList<>();

  @SuppressFBWarnings
  @Override
  public BitBucketPPRServerActor getServerActor() {
    return actor;
  }

  @SuppressFBWarnings
  @Override
  public BitBucketPPRServerPullRequest getServerPullRequest() {
    return pullRequest;
  }

  @Override
  public List<BitBucketPPRServerChange> getServerChanges() {
    return new ArrayList<>(changes);
  }

  @Override
  public String toString() {
    return new com.google.gson.Gson().toJson(this);
  }
}
