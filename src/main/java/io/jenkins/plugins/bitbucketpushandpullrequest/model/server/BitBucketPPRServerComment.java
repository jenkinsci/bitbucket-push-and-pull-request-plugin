package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serializable;

public class BitBucketPPRServerComment implements Serializable{
  private static final long serialVersionUID = -4778766502864544307L;

  String id;
  String text;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "BitBucketPPRServerComment [text=" + text + ", id=" + id + "]";
  }
}
