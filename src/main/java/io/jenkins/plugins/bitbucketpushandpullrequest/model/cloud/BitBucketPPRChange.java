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
import java.util.List;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPRChange implements Serializable {
  @Serial
  private static final long serialVersionUID = 7855401280893901392L;

  private boolean forced;
  private BitBucketPPRLinks links;
  private boolean truncated;
  private boolean created;
  private boolean closed;
  private List<BitBucketPPRCommit> commits;


  @SerializedName("new")
  private BitBucketPPRNew newChange;

  public boolean isForced() {
    return forced;
  }

  public List<BitBucketPPRCommit> getCommits() {
    return commits;
  }

  public void setCommits(List<BitBucketPPRCommit> commits) {
    this.commits = commits;
  }

  public void setForced(final boolean forced) {
    this.forced = forced;
  }

  public BitBucketPPRLinks getLinks() {
    return links;
  }

  public void setLinks(final BitBucketPPRLinks links) {
    this.links = links;
  }

  public boolean isTruncated() {
    return truncated;
  }

  public void setTruncated(final boolean truncated) {
    this.truncated = truncated;
  }


  public boolean getCreated() {
    return created;
  }

  public void setCreated(final boolean created) {
    this.created = created;
  }

  public boolean getClosed() {
    return closed;
  }

  public void setClosed(final boolean closed) {
    this.closed = closed;
  }

  public BitBucketPPRNew getNewChange() {
    return newChange;
  }

  public void setNewChange(final BitBucketPPRNew newChange) {
    this.newChange = newChange;
  }

  @Override
  public String toString() {
    return "BitBucketPPRChange [forced=" + forced + ", links=" + links + ", truncated=" + truncated
        + ", created=" + created + ", closed=" + closed + ", newChange=" + newChange + "]";
  }
}
