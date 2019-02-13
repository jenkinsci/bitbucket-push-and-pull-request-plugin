package io.jenkins.plugins.bitbucketpushandpullrequest.model;

import java.util.ArrayList;
import java.util.List;

public class BitBucketPPRPushFromServer {
  private List<BitBucketPPRChange> changes = new ArrayList<>();

  public List<BitBucketPPRChange> getChanges() {
    return changes;
  }

  public void setChanges(List<BitBucketPPRChange> changes) {
    this.changes = changes;
  }

  @Override
  public String toString() {
    return "Push [changes=" + changes + "]";
  }
}
