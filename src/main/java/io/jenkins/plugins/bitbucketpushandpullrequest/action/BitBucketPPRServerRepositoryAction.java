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
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerChange;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;

public class BitBucketPPRServerRepositoryAction extends InvisibleAction implements BitBucketPPRAction {
  private static final Logger logger = Logger.getLogger(BitBucketPPRAction.class.getName());

  private final @Nonnull BitBucketPPRPayload payload;
  private List<String> scmUrls = new ArrayList<>(2);
  private String targetBranchName = null;
  private String targetBranchRefId = null;
  private String type;

  public BitBucketPPRServerRepositoryAction(BitBucketPPRPayload payload) {
    this.payload = payload;

    // TODO: do we need link clones or link self is enough??
    List<BitBucketPPRServerClone> clones = payload.getServerRepository().getLinks().getCloneProperty();

    for (BitBucketPPRServerClone clone : clones) {
      if (clone.getName().equalsIgnoreCase("http") || clone.getName().equalsIgnoreCase("https")
          || clone.getName().equalsIgnoreCase("ssh")) {
        this.scmUrls.add(clone.getHref());
      }
    }

    for (BitBucketPPRServerChange change : payload.getServerChanges()) {
      if (change.getRefId() != null) {
        this.targetBranchName = change.getRef().getDisplayId();
        this.targetBranchRefId = change.getRefId();
        this.type = change.getRef().getType();
        break;
      }
    }

    logger.log(Level.INFO,
        () -> "Received commit hook notification from server for destination branch: " + this.targetBranchName);
    logger.log(Level.INFO, () -> "Received commit hook type from server: " + this.type);
  }

  @Override
  public String getTargetBranch() {
    return targetBranchName;
  }

  @Override
  public String getTargetBranchRefId() {
    return targetBranchRefId;
  }

  @Override
  public BitBucketPPRPayload getPayload() {
    return payload;
  }

  @Override
  public String getScm() {
    return payload.getServerRepository().getScmId();
  }

  @Override
  public String getUser() {
    return payload.getServerActor().getName();
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getRepositoryName() {
    return payload.getServerRepository().getName();
  }

  @Override
  public List<String> getScmUrls() {
    return scmUrls;
  }

  @Override
  public String getPullRequestId() {
    return null;
  }

  @Override
  public String getRepositoryId() {
    return payload.getServerRepository().getId();
  }

  @Override
  public String getRepositoryUrl() {
    return payload.getServerRepository().getLinks().getSelfProperty().get(0).getHref();
  }

  @Override
  public String getProjectUrl() {
    return payload.getServerRepository().getProject().getLinks().getSelfProperty().get(0).getHref();
  }

  @Override
  public String toString() {
    return "BitBucketPPRServerRepositoryAction";
  }

  @Override
  public String getLatestCommit() {
    // According to constructor `targetBranchName`, `type` and `targetBranchRefId` will be set to first non-null change
    // So lets hope it is not very destructive move to set latestCommit from first change.
    for (BitBucketPPRServerChange change : payload.getServerChanges()) {
      if (change.getRefId() != null) {
        return change.getToHash();
      }
    }
    return null;
  }

  @Override
  public List<String> getCommitLinks() {
    // returns:
    // /rest/build-status/1.0/commits/{commitId}

    String baseUrl = getBaseUrl(getProjectUrl());
    if (baseUrl == null) {
      logger.log(Level.WARNING, "Base url is empty.");
      return new ArrayList<String>();
    }

    List<BitBucketPPRServerChange> changes = payload.getServerChanges();
    List<String> links = new ArrayList<>();
    for (BitBucketPPRServerChange change : changes) {
      links.add(baseUrl + "/rest/build-status/1.0/commits/" + change.getToHash());
    }

    return links;
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
