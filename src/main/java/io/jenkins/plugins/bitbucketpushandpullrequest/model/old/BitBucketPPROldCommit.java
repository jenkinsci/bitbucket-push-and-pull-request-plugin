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
package io.jenkins.plugins.bitbucketpushandpullrequest.model.old;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;


public class BitBucketPPROldCommit implements Serializable {
  private String author;
  private String branch;
  private List<File> files = new ArrayList<>();
  private String message;
  private String node;
  private List<String> parents = new ArrayList<>();
  private @SerializedName("raw_author") String rawAuthor;
  private @SerializedName("raw_node") String rawNode;
  private int revision;
  private int size;
  private String timestamp;
  private String utctimestamp;

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public List<File> getFiles() {
    return files;
  }

  public void setFiles(List<File> files) {
    this.files = files;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  public List<String> getParents() {
    return parents;
  }

  public void setParents(List<String> parents) {
    this.parents = parents;
  }

  public String getRawAuthor() {
    return rawAuthor;
  }

  public void setRawAuthor(String rawAuthor) {
    this.rawAuthor = rawAuthor;
  }

  public String getRawNode() {
    return rawNode;
  }

  public void setRawNode(String rawNode) {
    this.rawNode = rawNode;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getUtctimestamp() {
    return utctimestamp;
  }

  public void setUtctimestamp(String utctimestamp) {
    this.utctimestamp = utctimestamp;
  }

  @Override
  public String toString() {
    return "BitBucketPPROldCommit [author=" + author + ", branch=" + branch + ", files=" + files + ", message="
        + message + ", node=" + node + ", parents=" + parents + ", rawAuthor=" + rawAuthor
        + ", rawNode=" + rawNode + ", revision=" + revision + ", size=" + size + ", timestamp="
        + timestamp + ", utctimestamp=" + utctimestamp + "]";
  }
}
