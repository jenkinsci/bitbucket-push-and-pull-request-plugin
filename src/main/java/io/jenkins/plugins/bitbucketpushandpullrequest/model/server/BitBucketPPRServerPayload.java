package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.util.ArrayList;
import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRServerPayload implements BitBucketPPRPayload {
  private static final long serialVersionUID = -5088466617368578337L;
  private BitBucketPPRServerActor actor;
  private BitBucketPPRServerPullRequest pullRequest;
  private BitBucketPPRServerRepository repository;
  private final List<BitBucketPPRServerChange> changes = new ArrayList<>();
  private BitBucketPPRServerComment comment;

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

  @SuppressFBWarnings
  @Override
  public BitBucketPPRServerRepository getServerRepository() {
    return repository;
  }

  @Override
  public List<BitBucketPPRServerChange> getServerChanges() {
    return new ArrayList<>(changes);
  }

  @Override
  public BitBucketPPRServerComment getServerComment() {
    return comment;
  }

  @Override
  public String toString() {
    return new com.google.gson.Gson().toJson(this);
  }
}
