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

import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRChange;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class BitBucketPPRRepositoryAction extends BitBucketPPRActionAbstract
    implements BitBucketPPRAction {
  private static final Logger logger = Logger.getLogger(BitBucketPPRAction.class.getName());
  public static final String COMMIT = "commit";
  private static final String BITBUCKET_API_BASE_URL = "https://api.bitbucket.org/2.0";
  private static final String BITBUCKET_REPOSITORIES = "repositories";

  private final @Nonnull BitBucketPPRPayload payload;
  private final String repoSlug;
  private final String workspace;

  private String targetBranchName;

  private String type;

  private String repositoryUuid;

  public BitBucketPPRRepositoryAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;

    for (BitBucketPPRChange change : payload.getPush().getChanges()) {
      if (change.getNewChange() != null) {
        this.targetBranchName = change.getNewChange().getName();
        this.type = change.getNewChange().getType();
        this.repositoryUuid = payload.getRepository().getUuid();
        break;
      }
    }

    Map<String, String> workspaceRepo;
    try {
      workspaceRepo =
          BitBucketPPRUtils.extractRepositoryNameFromHTTPSUrl(
              payload.getRepository().getLinks().getHtml().getHref());
    } catch (BitBucketPPRRepositoryNotParsedException e) {
      throw new RuntimeException(e);
    }
    this.repoSlug = workspaceRepo.get(BitBucketPPRUtils.BB_REPOSITORY);
    this.workspace = workspaceRepo.get(BitBucketPPRUtils.BB_WORKSPACE);

    logger.log(
        Level.INFO, () -> "Received commit hook notification for branch: " + this.targetBranchName);
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
    List<String> res = new ArrayList<>();
    res.add(payload.getRepository().getLinks().getHtml().getHref());
    return res;
  }

  @Override
  public String getRepositoryId() {
    return repositoryUuid;
  }

  @Override
  public String getLatestCommit() {
    // According to constructor `targetBranchName`, `type` and `repositoryUuid` will be set to first
    // non-null change
    // So lets hope it is not very destructive move to set latestCommit from first change.
    for (BitBucketPPRChange change : payload.getPush().getChanges()) {
      if (change.getNewChange() != null) {
        return change.getNewChange().getTarget().getHash();
      }
    }

    return null;
  }

  @Override
  public List<String> getCommitLinks() {
    String baseCommitLink =
        isEmpty(this.getPropagationUrl()) ? BITBUCKET_API_BASE_URL : this.getPropagationUrl();
    return payload.getPush().getChanges().stream()
        .map(
            c ->
                String.join(
                    "/",
                    baseCommitLink,
                    BITBUCKET_REPOSITORIES,
                    workspace,
                    repoSlug,
                    COMMIT,
                    c.getNewChange().getTarget().getHash()))
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "BitBucketPPRRepositoryAction";
  }
}
