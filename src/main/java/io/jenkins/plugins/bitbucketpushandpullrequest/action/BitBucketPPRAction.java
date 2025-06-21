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

import java.net.MalformedURLException;
import java.util.List;

import hudson.model.Action;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;

public interface BitBucketPPRAction extends Action {

  BitBucketPPRPayload getPayload();

  String getScm();

  default String getLinkHtml() {
    return null;
  }

  default String getLinkSelf() {
    return null;
  }

  default String getLinkApprove() throws MalformedURLException {
    return null;
  }

  default String getLinkDecline() throws MalformedURLException {
    return null;
  }

  default String getLinkStatuses() {
    return null;
  }

  default String getUser() {
    return null;
  }

  default String getSourceBranch() {
    return null;
  }

  default String getTargetBranch() {
    return null;
  }

  default String getTargetBranchRefId() {
    return null;
  }

  default String getType() {
    return null;
  }

  default String getRepositoryName() {
    return null;
  }

  // TODO: do we really neeed it?
  default List<String> getScmUrls() {
    return null;
  }

  default String getPullRequestId() {
    return null;
  }

  default String getRepositoryId() {
    return null;
  }

  default String getRepositoryUrl() {
    return null;
  }

  default String getProjectUrl() {
    return null;
  }

  default String getPullRequestApiUrl() {
    return null;
  }

  default String getPullRequestUrl() {
    return null;
  }

  default String getTitle() {
    return null;
  }

  default String getDescription() {
    return null;
  }

  default String getComment() {
    return null;
  }

  default String getServerComment() {
    return null;
  }

  default String getLatestCommit() {
    return null;
  }

  default String getCommitLink() throws MalformedURLException {
    return null;
  }

  default List<String> getCommitLinks() throws MalformedURLException {
    return null;
  }

  default String getLatestCommitFromRef() {
    return null;
  }

  default String getLatestCommitToRef() {
    return null;
  }
}
