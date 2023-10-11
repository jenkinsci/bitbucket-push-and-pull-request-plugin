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

import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRUtils;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRRepositoryNotParsedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import hudson.model.InvisibleAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import static java.util.Objects.nonNull;

public class BitBucketPPRPullRequestAction extends InvisibleAction implements BitBucketPPRAction {
  private static final Logger logger = Logger.getLogger(BitBucketPPRPullRequestAction.class.getName());
  public static final String APPROVE = "/approve";
  public static final String DECLINE = "/decline";
  public static final String STATUSES = "/statuses";
  public static final String COMMIT = "commit";
  public static final String BITBUCKET_API_BASE_URL = "https://api.bitbucket.org/2.0";
  public static final String BITBUCKET_HTTP_BASE_URL = "https://bitbucket.org";

  private static final String PULL_REQUEST_API = "pullrequests";

  private static final String PULL_REQUEST_HTTP = "pull-requests";
  private static final String BITBUCKET_REPOSITORIES = "repositories";
  private final @Nonnull BitBucketPPRPayload payload;
  private final @Nonnull String workspace;

  private final @Nonnull String repoSlug;

  private final @Nonnull String pullRequestId;

  public BitBucketPPRPullRequestAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;
    this.pullRequestId = payload.getPullRequest().getId();

    Map<String, String> workspaceRepo;
    try {
      workspaceRepo = BitBucketPPRUtils.extractRepositoryNameFromHTTPSUrl(
          payload.getRepository().getLinks().getHtml().getHref());
    } catch (BitBucketPPRRepositoryNotParsedException e) {
      throw new RuntimeException(e);
    }
    this.repoSlug = workspaceRepo.get(BitBucketPPRUtils.BB_REPOSITORY);
    this.workspace = workspaceRepo.get(BitBucketPPRUtils.BB_WORKSPACE);
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
  public String getPullRequestApiUrl() {
    return String.join("/", BITBUCKET_API_BASE_URL, BITBUCKET_REPOSITORIES, workspace, repoSlug,
        PULL_REQUEST_API, pullRequestId);
  }

  @Override
  public String getPullRequestUrl() {
    return String.join("/", BITBUCKET_HTTP_BASE_URL, workspace, repoSlug, PULL_REQUEST_HTTP,
        pullRequestId);
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
    String scm =
        nonNull(payload.getRepository().getScm()) ? payload.getRepository().getScm() : "git";
    if (!scm.equalsIgnoreCase("git")) {
      logger.log(Level.WARNING, "Payload received from SCM other than git.");
    }

    return payload.getRepository().getScm();
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
    List<String> res = new ArrayList<>();
    res.add(payload.getRepository().getLinks().getHtml().getHref());
    return res;
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
    return String.join("/", BITBUCKET_API_BASE_URL, BITBUCKET_REPOSITORIES, workspace, repoSlug,
        PULL_REQUEST_API, pullRequestId) + APPROVE;
  }

  @Override
  public String getLinkDecline() {
    return String.join("/", BITBUCKET_API_BASE_URL, BITBUCKET_REPOSITORIES, workspace, repoSlug,
        PULL_REQUEST_API, pullRequestId) + DECLINE;
  }

  @Override
  public String getLinkStatuses() {
    return String.join("/", BITBUCKET_API_BASE_URL, BITBUCKET_REPOSITORIES, workspace, repoSlug,
        PULL_REQUEST_API, pullRequestId) + STATUSES;
  }

  @Override
  public String getLatestCommit() {
    return payload.getPullRequest().getSource().getCommit().getHash();
  }

  @Override
  public String getCommitLink() {
    return String.join("/", BITBUCKET_API_BASE_URL, BITBUCKET_REPOSITORIES, workspace, repoSlug,
        COMMIT) + '/' + this.getLatestCommit();
  }

  @Override
  public String toString() {
    return "BitBucketPPRPullRequestAction";
  }
}
