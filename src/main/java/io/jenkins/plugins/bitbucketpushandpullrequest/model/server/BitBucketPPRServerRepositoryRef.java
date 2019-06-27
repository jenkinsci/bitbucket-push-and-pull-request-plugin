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

public class BitBucketPPRServerRepositoryRef implements Serializable {

  private String id;
  private String displayId;
  private String latestCommit;
  private BitBucketPPRServerRepository repository;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayId() {
    return displayId;
  }

  public void setDisplayId(String displayId) {
    this.displayId = displayId;
  }

  public String getLatestCommit() {
    return latestCommit;
  }

  public void setLatestCommit(String latestCommit) {
    this.latestCommit = latestCommit;
  }

  public BitBucketPPRServerRepository getRepository() {
    return repository;
  }

  public void setRepository(BitBucketPPRServerRepository repository) {
    this.repository = repository;
  }

  @Override
  public String toString() {
    return "BitBucketPPRServerRepositoryRef [id=" + id + ", displayId=" + displayId
        + ", latestCommit=" + latestCommit + ", repository=" + repository + "]";
  }
}
