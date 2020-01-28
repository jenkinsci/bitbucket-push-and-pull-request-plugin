package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serializable;


public class BitBucketPPRServerSelf implements Serializable {
  private static final long serialVersionUID = 1433703376294956482L;
  private String href;

  public String getHref() {
    return href;
  }

  public void setHref(final String href) {
    this.href = href;
  }

  @Override
  public String toString() {
    return "BitBucketPPRSelf [href=" + href + "]";
  }
}
