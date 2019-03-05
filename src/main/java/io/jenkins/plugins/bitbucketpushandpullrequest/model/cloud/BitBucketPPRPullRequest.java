/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;


public class BitBucketPPRPullRequest implements Serializable {
  private String id;
  private String title;
  private String description;
  private String state;
  private BitBucketPPRActor author;
  private @SerializedName("created_on") Date createdOn;
  private @SerializedName("updated_on") Date updatedOn;
  private BitBucketPPRSource source;
  private BitBucketPPRDestination destination;
  private List<BitBucketPPRParticipant> participants = new ArrayList<>();
  private String type;
  private String reason;
  private BitBucketPPRLinks links;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public BitBucketPPRActor getAuthor() {
    return author;
  }

  public void setAuthor(BitBucketPPRActor author) {
    this.author = author;
  }

  public BitBucketPPRSource getSource() {
    return source;
  }

  public void setSource(BitBucketPPRSource source) {
    this.source = source;
  }

  public BitBucketPPRDestination getDestination() {
    return destination;
  }

  public void setDestination(BitBucketPPRDestination destination) {
    this.destination = destination;
  }

  public BitBucketPPRLinks getLinks() {
    return links;
  }

  public void setLinks(BitBucketPPRLinks links) {
    this.links = links;
  }

  public List<BitBucketPPRParticipant> getParticipants() {
    return participants;
  }

  public void setParticipants(List<BitBucketPPRParticipant> participants) {
    this.participants = participants;
  }

  public Date getCreatedOn() {
    return (Date) createdOn.clone();
  }

  public void setCreatedOn(final Date createdOn) {
    this.createdOn = new Date(createdOn.getTime());
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

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  @Override
  public String toString() {
    return "BitBucketPPRPullRequest [id=" + id + ", title=" + title + ", description=" + description
        + ", state=" + state + ", author=" + author + ", createdOn=" + createdOn + ", updatedOn="
        + updatedOn + ", source=" + source + ", destination=" + destination + ", participants="
        + participants + ", type=" + type + ", reason=" + reason + ", links=" + links + "]";
  }
}
