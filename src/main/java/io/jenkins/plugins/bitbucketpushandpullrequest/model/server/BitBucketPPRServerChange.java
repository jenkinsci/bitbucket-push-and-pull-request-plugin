package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;


public class BitBucketPPRServerChange implements Serializable {
  private BitBucketPPRServerRef ref;
  private String refId;
  private String fromHash;
  private String toHash;
  private String type;

  public BitBucketPPRServerRef getRef() {
    return ref;
  }

  public void setRef(BitBucketPPRServerRef ref) {
    this.ref = ref;
  }

  public String getRefId() {
    return refId;
  }

  public void setRefId(String refId) {
    this.refId = refId;
  }

  public String getFromHash() {
    return fromHash;
  }

  public void setFromHash(String fromHash) {
    this.fromHash = fromHash;
  }

  public String getToHash() {
    return toHash;
  }

  public void setToHash(String toHash) {
    this.toHash = toHash;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
