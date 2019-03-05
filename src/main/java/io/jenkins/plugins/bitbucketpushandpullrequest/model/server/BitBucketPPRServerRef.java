package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serializable;


public class BitBucketPPRServerRef implements Serializable {
  private String id;
  private String displayId;
  private String type;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayId() {
    return displayId;
  }

  public void setDisplayId(String displayId) {
    this.displayId = displayId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
