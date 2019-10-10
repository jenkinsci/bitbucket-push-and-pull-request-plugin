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

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPRRepository implements Serializable {
  String scm;
  String website;
  String name;
  BitBucketPPRLinks links;
  BitBucketPPRProject project;
  BitBucketPPROwner owner;
  String fullName;
  String type;

  @SerializedName("is_private")
  Boolean isPrivate;
  String uuid;

  public String getScm() {
    return scm;
  }

  public void setScm(String scm) {
    this.scm = scm;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getIsPrivate() {
    return isPrivate;
  }

  public void setIsPrivate(Boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public BitBucketPPRLinks getLinks() {
    return links;
  }

  public void setLinks(BitBucketPPRLinks links) {
    this.links = links;
  }

  public BitBucketPPRProject getProject() {
    return project;
  }

  public void setProject(BitBucketPPRProject project) {
    this.project = project;
  }

  public BitBucketPPROwner getOwner() {
    return owner;
  }

  public void setOwner(BitBucketPPROwner owner) {
    this.owner = owner;
  }

  @Override
  public String toString() {
    return "BitBucketPPRRepository [scm=" + scm + ", website=" + website + ", name=" + name
        + ", links=" + links + ", project=" + project + ", owner=" + owner + ", fullName="
        + fullName + ", type=" + type + ", isPrivate=" + isPrivate + ", uuid=" + uuid + "]";
  }
}
