/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import java.io.Serial;
import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPRRepository implements Serializable {
  @Serial
  private static final long serialVersionUID = -5358049446460018798L;
  private String scm;
  private String website;
  private String name;
  private BitBucketPPRLinks links;
  private BitBucketPPRProject project;
  private BitBucketPPROwner owner;
  private String fullName;
  private String type;

  @SerializedName("is_private")
  private Boolean isPrivate;
  private String uuid;

  public String getScm() {
    return scm;
  }

  public void setScm(final String scm) {
    this.scm = scm;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(final String website) {
    this.website = website;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(final String fullName) {
    this.fullName = fullName;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public Boolean getIsPrivate() {
    return isPrivate;
  }

  public void setIsPrivate(final Boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(final String uuid) {
    this.uuid = uuid;
  }

  public BitBucketPPRLinks getLinks() {
    return links;
  }

  public void setLinks(final BitBucketPPRLinks links) {
    this.links = links;
  }

  public BitBucketPPRProject getProject() {
    return project;
  }

  public void setProject(final BitBucketPPRProject project) {
    this.project = project;
  }

  public BitBucketPPROwner getOwner() {
    return owner;
  }

  public void setOwner(final BitBucketPPROwner owner) {
    this.owner = owner;
  }

  @Override
  public String toString() {
    return "BitBucketPPRRepository [scm=" + scm + ", website=" + website + ", name=" + name
        + ", links=" + links + ", project=" + project + ", owner=" + owner + ", fullName="
        + fullName + ", type=" + type + ", isPrivate=" + isPrivate + ", uuid=" + uuid + "]";
  }
}
