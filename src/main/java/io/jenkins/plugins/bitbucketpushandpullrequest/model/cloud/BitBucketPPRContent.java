package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import java.io.Serializable;

public class BitBucketPPRContent implements Serializable {
  private static final long serialVersionUID = 970499631277905070L;

  String raw;
  String markup;
  String html;
  String type;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public String getMarkup() {
    return markup;
  }

  public void setMarkup(String markup) {
    this.markup = markup;
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "BitBucketPPRContent [html=" + html + ", markup=" + markup + ", raw=" + raw + ", type="
        + type + "]";
  } 
}
