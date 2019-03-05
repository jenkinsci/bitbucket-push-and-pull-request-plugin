package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import java.io.Serializable;

public class BitBucketPPRSelf implements Serializable {
  private String href;

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  @Override
  public String toString() {
    return "BitBucketPPRSelf [href=" + href + "]";
  }
}
