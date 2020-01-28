package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPRServerProject implements Serializable {
  private static final long serialVersionUID = -9104674017591226510L;
  private String key;
  private String id;
  private String name;

  @SerializedName("public")
  private boolean isPublic;
  private String type;

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }
}
