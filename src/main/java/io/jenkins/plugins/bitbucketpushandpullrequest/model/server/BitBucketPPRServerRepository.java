/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2025, Christian Del Monte.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/


package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serial;
import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public class BitBucketPPRServerRepository implements Serializable {
  @Serial
  private static final long serialVersionUID = 2888690501986298784L;
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

  public void setSlug(final String slug) {
    this.slug = slug;
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

  public String getScmId() {
    return scmId;
  }

  public void setScmId(final String scmId) {
    this.scmId = scmId;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatusMessage(final String statusMessage) {
    this.statusMessage = statusMessage;
  }

  public boolean isForkable() {
    return forkable;
  }

  public void setForkable(final boolean forkable) {
    this.forkable = forkable;
  }

  public BitBucketPPRServerProject getProject() {
    return project;
  }

  public void setProject(final BitBucketPPRServerProject project) {
    this.project = project;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }

  public BitBucketPPRServerLinks getLinks() {
    return links;
  }

  public void setLinks(final BitBucketPPRServerLinks links) {
    this.links = links;
  }

  @Override
  public String toString() {
    return "BitBucketPPRServerRepository [slug=" + slug + ", id=" + id + ", name=" + name
        + ", scmId=" + scmId + ", state=" + state + ", statusMessage=" + statusMessage
        + ", forkable=" + forkable + ", project=" + project + ", links=" + links + ", isPublic="
        + isPublic + "]";
  }
}
