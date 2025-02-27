package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serial;
import java.io.Serializable;


public class BitBucketPPRServerChange implements Serializable {
  @Serial
  private static final long serialVersionUID = 49898612250869977L;
  private BitBucketPPRServerRef ref;
  private String refId;
  private String fromHash;
  private String toHash;
  private String type;

  public BitBucketPPRServerRef getRef() {
    return ref;
  }

  public void setRef(final BitBucketPPRServerRef ref) {
    this.ref = ref;
  }

  public String getRefId() {
    return refId;
  }

  public void setRefId(final String refId) {
    this.refId = refId;
  }

  public String getFromHash() {
    return fromHash;
  }

  public void setFromHash(final String fromHash) {
    this.fromHash = fromHash;
  }

  public String getToHash() {
    return toHash;
  }

  public void setToHash(final String toHash) {
    this.toHash = toHash;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }
}
