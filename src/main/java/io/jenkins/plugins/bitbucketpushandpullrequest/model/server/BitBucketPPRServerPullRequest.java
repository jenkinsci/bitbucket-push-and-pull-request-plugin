/*******************************************************************************
 * The MIT License
 * <p>
 * Copyright (C) 2018-2019, CloudBees, Inc.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/


package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


public class BitBucketPPRServerPullRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -2700597086308321013L;
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

  private BitBucketPPRServerPRProperties properties;

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(final Long version) {
    this.version = version;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public Boolean getOpen() {
    return open;
  }

  public void setOpen(final Boolean open) {
    this.open = open;
  }

  public Boolean getClosed() {
    return closed;
  }

  public void setClosed(final Boolean closed) {
    this.closed = closed;
  }

  public Long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(final Long createdDate) {
    this.createdDate = createdDate;
  }

  public Long getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(final Long updatedDate) {
    this.updatedDate = updatedDate;
  }

  public Boolean getLocked() {
    return locked;
  }

  public void setLocked(final Boolean locked) {
    this.locked = locked;
  }

  public BitBucketPPRServerActor getAuthor() {
    return author;
  }

  public void setAuthor(final BitBucketPPRServerActor author) {
    this.author = author;
  }

  public List<BitBucketPPRServerActor> getReviewers() {
    return reviewers;
  }

  public void setReviewers(final List<BitBucketPPRServerActor> reviewers) {
    this.reviewers = reviewers;
  }

  public List<BitBucketPPRServerActor> getParticipants() {
    return participants;
  }

  public void setParticipants(final List<BitBucketPPRServerActor> participants) {
    this.participants = participants;
  }

  public BitBucketPPRServerLinks getLinks() {
    return links;
  }

  public void setLinks(final BitBucketPPRServerLinks links) {
    this.links = links;
  }

  public BitBucketPPRServerRepositoryRef getFromRef() {
    return fromRef;
  }

  public void setFromRef(final BitBucketPPRServerRepositoryRef fromRef) {
    this.fromRef = fromRef;
  }

  public BitBucketPPRServerRepositoryRef getToRef() {
    return toRef;
  }

  public void setToRef(final BitBucketPPRServerRepositoryRef toRef) {
    this.toRef = toRef;
  }

  public BitBucketPPRServerPRProperties getProperties() {
    return properties;
  }

  public void setToRef(final BitBucketPPRServerPRProperties properties) {
    this.properties = properties;
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
