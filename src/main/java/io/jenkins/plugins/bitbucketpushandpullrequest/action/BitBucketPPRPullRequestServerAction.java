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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import hudson.model.InvisibleAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;

public class BitBucketPPRPullRequestServerAction extends InvisibleAction implements BitBucketPPRAction {
  private static final Logger logger = Logger.getLogger(BitBucketPPRPullRequestServerAction.class.getName());

  private final @Nonnull BitBucketPPRPayload payload;
  private List<String> scmUrls = new ArrayList<>(2);
  private String repositoryUuid;

  public BitBucketPPRPullRequestServerAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;

    // TODO: do we need link clones or link self is enough??
    List<BitBucketPPRServerClone> clones = payload.getServerPullRequest().getToRef().getRepository().getLinks()
        .getCloneProperty();

    for (BitBucketPPRServerClone clone : clones) {
      if (clone.getName().equalsIgnoreCase("http") || clone.getName().equalsIgnoreCase("https")
          || clone.getName().equalsIgnoreCase("ssh")) {
        this.scmUrls.add(clone.getHref());
      }
    }

    logger.fine("BitBucketPPRPullRequestServerAction was called.");
  }

  @Override
  public String getSourceBranch() {
    return payload.getServerPullRequest().getFromRef().getDisplayId();
  }

  @Override
  public String getTargetBranch() {
    return payload.getServerPullRequest().getToRef().getDisplayId();
  }

  @Override
  public String getPullRequestUrl() {
    return payload.getServerPullRequest().getLinks().getSelfProperty().get(0).getHref();
  }

  @Override
  public String getScm() {
    return payload.getServerPullRequest().getFromRef().getRepository().getScmId();
  }

  @Override
  public String getUser() {
    return payload.getServerActor().getName();
  }

  @Override
  public String getTitle() {
    return payload.getServerPullRequest().getTitle();
  }

  @Override
  public BitBucketPPRPayload getPayload() {
    return payload;
  }

  @Override
  public String getRepositoryName() {
    return payload.getServerRepository().getName();
  }

  @Override
  public String getServerComment() {
    return payload.getServerComment().getText();
  }

  @Override
  public List<String> getScmUrls() {
    return scmUrls;
  }

  @Override
  public String getPullRequestId() {
    return Long.toString(payload.getServerPullRequest().getId());
  }

  @Override
  public String getRepositoryId() {
    return repositoryUuid;
  }

  @Override
  public String getLinkApprove() {
    // returns:
    // {baseUrl}/rest/api/1.0/projects/{projectKey}/repos/{repositorySlug}/pull-requests/{pullRequestId}/approve

    String projectKey = payload.getServerPullRequest().getFromRef().getRepository().getProject().getKey();
    String repoSlug = payload.getServerPullRequest().getFromRef().getRepository().getSlug();
    Long pullrequestId = payload.getServerPullRequest().getId();

    String projectSelfUrl = payload.getServerPullRequest().getFromRef().getRepository().getProject().getLinks()
        .getSelfProperty().get(0).getHref();
    String baseUrl = getBaseUrl(projectSelfUrl);
    if (baseUrl == null) {
      logger.log(Level.WARNING, "Base url is empty.");
      return "";
    }

    return baseUrl + "/rest/api/1.0/projects/" + projectKey.trim() + "/repos/" + repoSlug.trim() + "/pull-requests/"
        + Long.toString(pullrequestId).trim() + "/approve";
  }

  @Override
  public String getLinkDecline() {
    // returns:
    // {baseUrl}/rest/api/1.0/projects/{projectKey}/repos/{repositorySlug}/pull-requests/{pullRequestId}/approve

    String projectKey = payload.getServerPullRequest().getFromRef().getRepository().getProject().getKey();
    String repoSlug = payload.getServerPullRequest().getFromRef().getRepository().getSlug();
    Long pullrequestId = payload.getServerPullRequest().getId();

    String projectSelfUrl = payload.getServerPullRequest().getFromRef().getRepository().getProject().getLinks()
        .getSelfProperty().get(0).getHref();
    String baseUrl = getBaseUrl(projectSelfUrl);
    if (baseUrl == null) {
      logger.log(Level.WARNING, "Base url is empty.");
      return "";
    }

    return baseUrl + "/rest/api/1.0/projects/" + projectKey.trim() + "/repos/" + repoSlug.trim() + "/pull-requests/"
        + Long.toString(pullrequestId).trim() + "/decline";
  }

  @Override
  public String getCommitLink() {
    // returns:
    // /rest/build-status/1.0/commits/{commitId}
    String projectSelfUrl = payload.getServerPullRequest().getFromRef().getRepository().getProject().getLinks()
        .getSelfProperty().get(0).getHref();
    String commitId = payload.getServerPullRequest().getFromRef().getLatestCommit();

    String baseUrl = getBaseUrl(projectSelfUrl);
    if (baseUrl == null) {
      logger.log(Level.WARNING, "Base url is empty.");
      return "";
    }

    String result = baseUrl + "/rest/build-status/1.0/commits/" + commitId;
    return result;
  }

  @Override
  public String toString() {
    return "BitBucketPPRPullRequestServerAction";
  }

  private String getBaseUrl(String projectSelfUrl) {
    String baseUrl = null;
    try {
      URL url = new URL(projectSelfUrl);
      baseUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
    } catch (MalformedURLException e) {
      logger.log(Level.WARNING, "Cannot extract base url", e);
    }
    return baseUrl;
  }

}
