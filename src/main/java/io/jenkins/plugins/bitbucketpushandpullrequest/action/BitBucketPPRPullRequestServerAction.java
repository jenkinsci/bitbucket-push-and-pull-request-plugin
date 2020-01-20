/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2019, CloudBees, Inc.
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
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;


public class BitBucketPPRPullRequestServerAction extends BitBucketPPRAction {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRPullRequestServerAction.class.getName());

  public BitBucketPPRPullRequestServerAction(@Nonnull BitBucketPPRPayload payload) {
    super(payload);
    this.pullRequestId = Long.toString(payload.getServerPullRequest().getId());


    this.scm = payload.getServerPullRequest().getFromRef().getRepository().getScmId();

    List<BitBucketPPRServerClone> clones =
        payload.getServerPullRequest().getToRef().getRepository().getLinks().getCloneProperty();

    for (BitBucketPPRServerClone clone : clones) {
      if (clone.getName().equalsIgnoreCase("http") || clone.getName().equalsIgnoreCase("ssh")) {
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

  public String getPullRequestUrl() {
    return payload.getServerPullRequest().getLinks().getSelfProperty().get(0).getHref();
  }

  @Override
  public String getScm() {
    return payload.getServerPullRequest().getFromRef().getRepository().getScmId();
  }

  public String getTitle() {
    return payload.getPullRequest().getTitle();
  }

  public String getDescription() {
    return payload.getPullRequest().getDescription();
  }
}
