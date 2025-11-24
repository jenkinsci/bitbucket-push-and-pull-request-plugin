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

package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class BitBucketPPRComment implements Serializable {
  @Serial
  private static final long serialVersionUID = -8486598082322838487L;

  String id;
  BitBucketPPRLinks links;
  Boolean deleted;
  BitBucketPPRPullRequest pullrequest;
  BitBucketPPRContent content;
  Date createdOn;
  BitBucketPPROwner user;
  Date updatedOn;
  String type;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public BitBucketPPRLinks getLinks() {
    return links;
  }

  public void setLinks(BitBucketPPRLinks links) {
    this.links = links;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public BitBucketPPRPullRequest getPullrequest() {
    return pullrequest;
  }

  public void setPullrequest(BitBucketPPRPullRequest pullrequest) {
    this.pullrequest = pullrequest;
  }

  public BitBucketPPRContent getContent() {
    return content;
  }

  public void setContent(BitBucketPPRContent content) {
    this.content = content;
  }

  public Date getCreatedOn() {
    return (Date) createdOn.clone();
  }

  public void setCreatedOn(final Date createdOn) {
    this.createdOn = new Date(createdOn.getTime());
  }

  public BitBucketPPROwner getUser() {
    return user;
  }

  public void setUser(BitBucketPPROwner user) {
    this.user = user;
  }

  public Date getUpdatedOn() {
    return (Date) updatedOn.clone();
  }

  public void setUpdatedOn(final Date updatedOn) {
    this.updatedOn = new Date(updatedOn.getTime());
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "BitBucketPPRComment [content=" + content + ", createdOn=" + createdOn + ", deleted="
        + deleted + ", id=" + id + ", links=" + links + ", pullrequest=" + pullrequest + ", type="
        + type + ", updatedOn=" + updatedOn + ", user=" + user + "]";
  }
}
