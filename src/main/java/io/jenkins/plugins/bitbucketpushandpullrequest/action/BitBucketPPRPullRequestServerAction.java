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
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import hudson.model.InvisibleAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;

public class BitBucketPPRPullRequestServerAction extends InvisibleAction implements BitBucketPPRAction {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRPullRequestServerAction.class.getName());

  private final @Nonnull BitBucketPPRPayload payload;
  private List<String> scmUrls = new ArrayList<>(2);
  private String repositoryUuid;

  public BitBucketPPRPullRequestServerAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;

    
    // TODO: do we need link clones or link self is enough??
    List<BitBucketPPRServerClone> clones =
        payload.getServerPullRequest().getToRef().getRepository().getLinks().getCloneProperty();

    for (BitBucketPPRServerClone clone : clones) {
      if (clone.getName().equalsIgnoreCase("http")
        || clone.getName().equalsIgnoreCase("https")
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
  public String getType() {
    return null;
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
    return Long.toString(payload.getServerPullRequest().getId());
  }

  @Override
  public String getRepositoryId() {
    return repositoryUuid;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getComment() {
    return null;
  }

  @Override
  public String getLinkHtml() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLinkSelf() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLinkApprove() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLinkDecline() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLinkStatuses() {
    // TODO Auto-generated method stub
    return null;
  }
}
