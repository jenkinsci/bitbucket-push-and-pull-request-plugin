/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2020, CloudBees, Inc.
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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import java.util.List;

import hudson.model.Action;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;

public interface BitBucketPPRAction extends Action {

  public BitBucketPPRPayload getPayload();

  public String getScm();

  public default String getLinkHtml() {
    return null;
  }

  public default String getLinkSelf() {
    return null;
  }

  public default String getLinkApprove() {
    return null;
  }

  public default String getLinkDecline() {
    return null;
  }

  public default String getLinkStatuses() {
    return null;
  }

  public default String getUser() {
    return null;
  }

  public default String getSourceBranch() {
    return null;
  }

  public default String getTargetBranch() {
    return null;
  }

  public default String getTargetBranchRefId() {
    return null;
  }

  public default String getType() {
    return null;
  }

  public default String getRepositoryName() {
    return null;
  }
  // TODO: do we really neeed it?
  public default List<String> getScmUrls() {
    return null;
  }

  public default String getPullRequestId() {
    return null;
  }

  public default String getRepositoryId(){
    return null;
  }

  public default String getPullRequestUrl(){
    return null;
  }

  public default String getTitle(){
    return null;
  }

  public default String getDescription(){
    return null;
  }

  public default String getComment(){
    return null;
  }

  public default String getServerComment(){
    return null;
  }

  public default String getCommitLink() {
    return null;
  }

  public default List<String> getCommitLinks() {
    return null;
  }
}
