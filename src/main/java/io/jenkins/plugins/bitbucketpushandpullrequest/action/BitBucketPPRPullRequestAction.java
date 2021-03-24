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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import hudson.model.InvisibleAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;

public class BitBucketPPRPullRequestAction extends InvisibleAction implements BitBucketPPRAction {

  private final @Nonnull BitBucketPPRPayload payload;
  private List<String> scmUrls = new ArrayList<>(2);

  public BitBucketPPRPullRequestAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;

    // TODO Why??
    scmUrls.add(payload.getRepository().getLinks().getHtml().getHref());
  }

  @Override
  public String getSourceBranch() {
    return payload.getPullRequest().getSource().getBranch().getName();
  }

  @Override
  public String getTargetBranch() {
    return payload.getPullRequest().getDestination().getBranch().getName();
  }
  
  @Override
  public String getLatestCommitFromRef() {
    return payload.getPullRequest().getSource().getCommit().getHash();
  }
  
  @Override
  public String getLatestCommitToRef() {
    return payload.getPullRequest().getDestination().getCommit().getHash();
  }

  @Override
  public String getPullRequestUrl() {
    return payload.getPullRequest().getLinks().getHtml().getHref();
  }

  @Override
  public String getTitle() {
    return payload.getPullRequest().getTitle();
  }

  @Override
  public String getDescription() {
    return payload.getPullRequest().getDescription();
  }

  @Override
  public BitBucketPPRPayload getPayload() {
    return payload;
  }

  @Override
  public String getScm() {
    return payload.getRepository().getScm() != null ? payload.getRepository().getScm() : "git";
  }

  @Override
  public String getUser() {
    return payload.getActor().getNickname();
  }

  @Override
  public String getRepositoryName() {
    return payload.getRepository().getName();
  }

  @Override
  public List<String> getScmUrls() {
    return scmUrls;
  }

  @Override
  public String getPullRequestId() {
    return payload.getPullRequest().getId();
  }

  @Override
  public String getComment() {
    if (payload.getComment() == null || payload.getComment().getContent() == null
        || payload.getComment().getContent().getRaw() == null) {
      return "";
    }
    return payload.getComment().getContent().getRaw();
  }

  @Override
  public String getLinkHtml() {
    return payload.getPullRequest().getLinks().getHtml().getHref();
  }

  @Override
  public String getLinkSelf() {
    return payload.getPullRequest().getLinks().getSelf().getHref();
  }

  @Override
  public String getLinkApprove() {
    return payload.getPullRequest().getLinks().getApprove().getHref();
  }

  @Override
  public String getLinkDecline() {
    return payload.getPullRequest().getLinks().getDecline().getHref();
  }

  @Override
  public String getLinkStatuses() {
    return payload.getPullRequest().getLinks().getStatuses().getHref();
  }

  @Override
  public String getLatestCommit() {
    return payload.getPullRequest().getSource().getCommit().getHash();
  }

  @Override
  public String getCommitLink() {
    return payload.getPullRequest().getSource().getCommit().getLinks().getSelf().getHref();
  }

  @Override
  public String toString() {
    return "BitBucketPPRPullRequestAction";
  }
}
