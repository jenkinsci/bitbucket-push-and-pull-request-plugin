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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

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
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRAction.class.getName());

  private final @Nonnull BitBucketPPRPayload payload;
  private String scm;
  private String user;
  private List<String> scmUrls = new ArrayList<>(2);
  private String sourceBranchName = null;
  private String targetBranchName = null;
  private String type;
  private String repositoryName;
  private String pullRequestId;
  private String repositoryUuid;

  public BitBucketPPRServerRepositoryAction(BitBucketPPRPayload payload) {
    user = payload.getServerActor().getName();

    // TODO
    // scmUrls.add(payload.getRepository().getLinks().getHtml().getHref());

    this.scm = payload.getServerRepository().getScmId();
    this.repositoryName = payload.getServerRepository().getName();

    List<BitBucketPPRServerClone> clones =
        payload.getServerRepository().getLinks().getCloneProperty();

    for (BitBucketPPRServerClone clone : clones) {
      if (clone.getName().equalsIgnoreCase("http") || clone.getName().equalsIgnoreCase("ssh")) {
        this.scmUrls.add(clone.getHref());
      }
    }

    for (BitBucketPPRServerChange change : payload.getServerChanges()) {
      if (change.getRefId() != null) {
        this.targetBranchName = change.getRef().getDisplayId();
        this.type = change.getType();
        break;
      }
    }

    LOGGER.log(Level.INFO,
        () -> "Received commit hook notification from server for destination branch: " + this.targetBranchName);
    LOGGER.log(Level.INFO, () -> "Received commit hook type from server: " + this.type);
  }

  @Override
  public String getTargetBranch() {
    return targetBranchName;
  }
}
