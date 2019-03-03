package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class BitBucketPPRServerRepository implements Serializable {
  private String slug;
  private String id;
  private String name;
  private String scmId;
  private String state;
  private String statusMessage;
  private boolean forkable;
  private BitBucketPPRServerProject project;
  private BitBucketPPRServerLinks links;
  

  @SerializedName("public")
  private boolean isPublic;

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
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

  public String getScmId() {
    return scmId;
  }

  public void setScmId(String scmId) {
    this.scmId = scmId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }

  public boolean isForkable() {
    return forkable;
  }

  public void setForkable(boolean forkable) {
    this.forkable = forkable;
  }

  public BitBucketPPRServerProject getProject() {
    return project;
  }

  public void setProject(BitBucketPPRServerProject project) {
    this.project = project;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public BitBucketPPRServerLinks getLinks() {
    return links;
  }

  public void setLinks(BitBucketPPRServerLinks links) {
    this.links = links;
  }
}
