package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import java.io.Serializable;

public class BitBucketPPRSelf implements Serializable {
  private static final long serialVersionUID = -9156939739440067961L;
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
