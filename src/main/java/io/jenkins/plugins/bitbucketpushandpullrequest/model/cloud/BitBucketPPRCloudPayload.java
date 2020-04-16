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


public class BitBucketPPRCloudPayload implements BitBucketPPRPayload {
  private static final long serialVersionUID = -3467640601880230847L;

  private BitBucketPPRPush push;

  private BitBucketPPRRepository repository;
  private BitBucketPPRActor actor;
  private @SerializedName("pullrequest") BitBucketPPRPullRequest pullRequest;
  private BitBucketPPRApproval approval;
  private BitBucketPPRComment comment;

  @Override
  public BitBucketPPRPush getPush() {
    return push;
  }

  public void setPush(final BitBucketPPRPush push) {
    this.push = push;
  }

  @Override
  public BitBucketPPRRepository getRepository() {
    return repository;
  }

  public void setRepository(final BitBucketPPRRepository repository) {
    this.repository = repository;
  }

  @Override
  public BitBucketPPRActor getActor() {
    return actor;
  }

  public void setActor(final BitBucketPPRActor actor) {
    this.actor = actor;
  }

  @Override
  public BitBucketPPRPullRequest getPullRequest() {
    return pullRequest;
  }

  public void setPullRequest(final BitBucketPPRPullRequest pullRequest) {
    this.pullRequest = pullRequest;
  }

  @Override
  public BitBucketPPRApproval getApproval() {
    return approval;
  }

  public void setApproval(final BitBucketPPRApproval approval) {
    this.approval = approval;
  }

  @Override
  public BitBucketPPRComment getComment() {
    return comment;
  }

  public void setComment(BitBucketPPRComment comment) {
    this.comment = comment;
  }

  @Override
  public String toString() {
    return new com.google.gson.Gson().toJson(this);
  }
}
