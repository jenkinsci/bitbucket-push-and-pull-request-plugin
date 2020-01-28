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
package io.jenkins.plugins.bitbucketpushandpullrequest.model.old;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPROldRepository implements Serializable {
  private static final long serialVersionUID = -8606310364160210335L;
  private @SerializedName("absolute_url") String absoluteUrl;
  private boolean fork;
  private @SerializedName("is_private") boolean isPrivate;
  private String name;
  private String owner;
  private String scm;
  private String slug;
  private String website;

  public String getAbsoluteUrl() {
    return absoluteUrl;
  }

  public void setAbsoluteUrl(final String absoluteUrl) {
    this.absoluteUrl = absoluteUrl;
  }

  public boolean isFork() {
    return fork;
  }

  public void setFork(final boolean fork) {
    this.fork = fork;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  public void setPrivate(final boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(final String owner) {
    this.owner = owner;
  }

  public String getScm() {
    return scm;
  }

  public void setScm(final String scm) {
    this.scm = scm;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(final String slug) {
    this.slug = slug;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(final String website) {
    this.website = website;
  }

  @Override
  public String toString() {
    return "BitBucketPPROldRepository [absoluteUrl=" + absoluteUrl + ", fork=" + fork
        + ", isPrivate=" + isPrivate + ", name=" + name + ", owner=" + owner + ", scm=" + scm
        + ", slug=" + slug + ", website=" + website + "]";
  }
}
