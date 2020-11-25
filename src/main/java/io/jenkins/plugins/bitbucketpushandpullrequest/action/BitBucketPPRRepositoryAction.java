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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import hudson.model.InvisibleAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRChange;


public class BitBucketPPRRepositoryAction extends InvisibleAction implements BitBucketPPRAction {
  private static final Logger logger = Logger.getLogger(BitBucketPPRAction.class.getName());

  private final @Nonnull BitBucketPPRPayload payload;

  private List<String> scmUrls = new ArrayList<>(2);

  private String targetBranchName;

  private String type;

  private String repositoryUuid;

  public BitBucketPPRRepositoryAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;

    // TODO: why??
    scmUrls.add(payload.getRepository().getLinks().getHtml().getHref());

    for (BitBucketPPRChange change : payload.getPush().getChanges()) {
      if (change.getNewChange() != null) {
        this.targetBranchName = change.getNewChange().getName();
        this.type = change.getNewChange().getType();
        this.repositoryUuid = payload.getRepository().getUuid();
        break;
      }
    }

    logger.log(Level.INFO,
        () -> "Received commit hook notification for branch: " + this.targetBranchName);
    logger.log(Level.INFO, () -> "Received commit hook type: " + this.type);
  }

  @Override
  public String getTargetBranch() {
    return targetBranchName;
  }

  @Override
  public String getRepositoryUrl() {
    return payload.getRepository().getLinks().getHtml().getHref();
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
  public String getType() {
    return type;
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
  public String getRepositoryId() {
    return repositoryUuid;
  }

  @Override
  public List<String> getCommitLinks() {
    List<BitBucketPPRChange> changes = payload.getPush().getChanges();
    List<String> links = new ArrayList<>();

    for (BitBucketPPRChange change : changes) {
      links.add(change.getNewChange().getTarget().getLinks().getSelf().getHref());
    }

    return links;
  }

  @Override 
  public String toString() {
    return "BitBucketPPRRepositoryAction";
  }
}
