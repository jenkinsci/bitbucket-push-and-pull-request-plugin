/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018-2019, CloudBees, Inc.
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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;


public class BitBucketPPRServerPullRequest implements Serializable {
  private Long id;
  private Long version;
  private String title;
  private String state;
  private Boolean open;
  private Boolean closed;
  private Long createdDate;
  private Long updatedDate;
  private Boolean locked;
  private BitBucketPPRServerActor author;

  private List<BitBucketPPRServerActor> reviewers;
  private List<BitBucketPPRServerActor> participants;

  private BitBucketPPRServerLinks links;

  private BitBucketPPRServerRepositoryRef fromRef;

  private BitBucketPPRServerRepositoryRef toRef;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Boolean getOpen() {
    return open;
  }

  public void setOpen(Boolean open) {
    this.open = open;
  }

  public Boolean getClosed() {
    return closed;
  }

  public void setClosed(Boolean closed) {
    this.closed = closed;
  }

  public Long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Long createdDate) {
    this.createdDate = createdDate;
  }

  public Long getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Long updatedDate) {
    this.updatedDate = updatedDate;
  }

  public Boolean getLocked() {
    return locked;
  }

  public void setLocked(Boolean locked) {
    this.locked = locked;
  }

  public BitBucketPPRServerActor getAuthor() {
    return author;
  }

  public void setAuthor(BitBucketPPRServerActor author) {
    this.author = author;
  }

  public List<BitBucketPPRServerActor> getReviewers() {
    return reviewers;
  }

  public void setReviewers(List<BitBucketPPRServerActor> reviewers) {
    this.reviewers = reviewers;
  }

  public List<BitBucketPPRServerActor> getParticipants() {
    return participants;
  }

  public void setParticipants(List<BitBucketPPRServerActor> participants) {
    this.participants = participants;
  }

  public BitBucketPPRServerLinks getLinks() {
    return links;
  }

  public void setLinks(BitBucketPPRServerLinks links) {
    this.links = links;
  }

  public BitBucketPPRServerRepositoryRef getFromRef() {
    return fromRef;
  }

  public void setFromRef(BitBucketPPRServerRepositoryRef fromRef) {
    this.fromRef = fromRef;
  }

  public BitBucketPPRServerRepositoryRef getToRef() {
    return toRef;
  }

  public void setToRef(BitBucketPPRServerRepositoryRef toRef) {
    this.toRef = toRef;
  }

  @Override
  public String toString() {
    return "BitBucketPPRServerPullRequest [id=" + id + ", version=" + version + ", title=" + title
        + ", state=" + state + ", open=" + open + ", closed=" + closed + ", createdDate="
        + createdDate + ", updatedDate=" + updatedDate + ", locked=" + locked + ", author=" + author
        + ", reviewers=" + reviewers + ", participants=" + participants + ", links=" + links
        + ", fromRef=" + fromRef + ", toRef=" + toRef + "]";
  }
}
