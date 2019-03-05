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

import com.google.gson.annotations.SerializedName;

import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRNewPayload implements BitBucketPPRPayload {
  private BitBucketPPRPush push;

  private BitBucketPPRRepository repository;
  private BitBucketPPRActor actor;
  private @SerializedName("pullrequest") BitBucketPPRPullRequest pullRequest;
  private BitBucketPPRApproval approval;


  public BitBucketPPRPush getPush() {
    return push;
  }

  public void setPush(BitBucketPPRPush push) {
    this.push = push;
  }

  public BitBucketPPRRepository getRepository() {
    return repository;
  }

  public void setRepository(BitBucketPPRRepository repository) {
    this.repository = repository;
  }

  public BitBucketPPRActor getActor() {
    return actor;
  }

  public void setActor(BitBucketPPRActor actor) {
    this.actor = actor;
  }

  public BitBucketPPRPullRequest getPullRequest() {
    return pullRequest;
  }

  public void setPullRequest(BitBucketPPRPullRequest pullRequest) {
    this.pullRequest = pullRequest;
  }

  public BitBucketPPRApproval getApproval() {
    return approval;
  }

  public void setApproval(BitBucketPPRApproval approval) {
    this.approval = approval;
  }
}
