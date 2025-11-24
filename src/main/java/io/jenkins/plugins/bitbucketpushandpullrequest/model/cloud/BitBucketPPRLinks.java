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
import com.google.gson.annotations.SerializedName;

public class BitBucketPPRLinks implements Serializable {
  @Serial
  private static final long serialVersionUID = 8607244117530188175L;

  private BitBucketPPRLinkHtml html;

  @SerializedName("self")
  private BitBucketPPRSelf selfProperty;

  private BitBucketPPRLinkDecline decline;
  private BitBucketPPRLinkDiffStat diffstat;
  private BitBucketPPRLinkCommits commits;
  private BitBucketPPRLinkComments comments;
  private BitBucketPPRLinkMerge merge;
  private BitBucketPPRLinkActivity activity;
  private BitBucketPPRLinkDiff diff;
  private BitBucketPPRLinkApprove approve;
  private BitBucketPPRLinkStatuses statuses;

  public BitBucketPPRLinkHtml getHtml() {
    return html;
  }

  public BitBucketPPRLinkStatuses getStatuses() {
    return statuses;
  }

  public void setStatuses(BitBucketPPRLinkStatuses statuses) {
    this.statuses = statuses;
  }

  public BitBucketPPRLinkApprove getApprove() {
    return approve;
  }

  public void setApprove(BitBucketPPRLinkApprove approve) {
    this.approve = approve;
  }

  public BitBucketPPRLinkDiff getDiff() {
    return diff;
  }

  public void setDiff(BitBucketPPRLinkDiff diff) {
    this.diff = diff;
  }

  public BitBucketPPRLinkActivity getActivity() {
    return activity;
  }

  public void setActivity(BitBucketPPRLinkActivity activity) {
    this.activity = activity;
  }

  public BitBucketPPRLinkMerge getMerge() {
    return merge;
  }

  public void setMerge(BitBucketPPRLinkMerge merge) {
    this.merge = merge;
  }

  public BitBucketPPRLinkComments getComments() {
    return comments;
  }

  public void setComments(BitBucketPPRLinkComments comments) {
    this.comments = comments;
  }

  public BitBucketPPRLinkCommits getCommits() {
    return commits;
  }

  public void setCommits(BitBucketPPRLinkCommits commits) {
    this.commits = commits;
  }

  public BitBucketPPRLinkDiffStat getDiffstat() {
    return diffstat;
  }

  public void setDiffstat(BitBucketPPRLinkDiffStat diffstat) {
    this.diffstat = diffstat;
  }

  public BitBucketPPRLinkDecline getDecline() {
    return decline;
  }

  public void setDecline(BitBucketPPRLinkDecline decline) {
    this.decline = decline;
  }

  public void setHtml(final BitBucketPPRLinkHtml html) {
    this.html = html;
  }

  public BitBucketPPRSelf getSelf() {
    return selfProperty;
  }

  public void setSelf(final BitBucketPPRSelf selfProperty) {
    this.selfProperty = selfProperty;
  }

  @Override
  public String toString() {
    return "BitBucketPPRLinks [html=" + html + ", selfProperty=" + selfProperty + "]";
  }
}
