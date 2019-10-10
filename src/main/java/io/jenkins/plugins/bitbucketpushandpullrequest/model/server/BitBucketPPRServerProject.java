package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPRServerProject implements Serializable {
  private String key;
  private String id;
  private String name;

  @SerializedName("public")
  private boolean isPublic;
  private String type;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
