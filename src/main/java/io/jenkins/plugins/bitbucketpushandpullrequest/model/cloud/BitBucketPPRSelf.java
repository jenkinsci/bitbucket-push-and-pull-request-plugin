package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

public class BitBucketPPRSelf {
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
