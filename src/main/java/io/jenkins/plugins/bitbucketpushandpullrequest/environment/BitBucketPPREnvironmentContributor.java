/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2021, CloudBees, Inc.
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

package io.jenkins.plugins.bitbucketpushandpullrequest.environment;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.Cause;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestServerAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.cloud.BitBucketPPRPullRequestCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.server.BitBucketPPRPullRequestServerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRUtils;

@Extension
public class BitBucketPPREnvironmentContributor extends EnvironmentContributor {

  static final String BITBUCKET_PULL_REQUEST_ID = "BITBUCKET_PULL_REQUEST_ID";
  static final String BITBUCKET_PULL_REQUEST_LINK = "BITBUCKET_PULL_REQUEST_LINK";
  static final String BITBUCKET_TARGET_BRANCH = "BITBUCKET_TARGET_BRANCH";
  static final String BITBUCKET_REPOSITORY_UUID = "BITBUCKET_REPOSITORY_UUID";
  static final String BITBUCKET_REPOSITORY_ID = "BITBUCKET_REPOSITORY_ID";
  static final String BITBUCKET_REPOSITORY_URL = "BITBUCKET_REPOSITORY_URL";
  static final String BITBUCKET_SOURCE_BRANCH = "BITBUCKET_SOURCE_BRANCH";
  static final String BITBUCKET_PULL_REQUEST_COMMENT_TEXT = "BITBUCKET_PULL_REQUEST_COMMENT_TEXT";
  static final String REPOSITORY_LINK = "REPOSITORY_LINK";
  static final String REPOSITORY_NAME = "REPOSITORY_NAME";
  static final String BITBUCKET_ACTOR = "BITBUCKET_ACTOR";
  static final String BITBUCKET_PULL_REQUEST_TITLE = "BITBUCKET_PULL_REQUEST_TITLE";
  static final String BITBUCKET_PULL_REQUEST_DESCRIPTION = "BITBUCKET_PULL_REQUEST_DESCRIPTION";
  static final String BITBUCKET_PAYLOAD = "BITBUCKET_PAYLOAD";
  static final String BITBUCKET_X_EVENT = "BITBUCKET_X_EVENT";
  static final String BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_SOURCE_BRANCH =
      "BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_SOURCE_BRANCH";
  static final String BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_TARGET_BRANCH =
      "BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_TARGET_BRANCH";

  static final Logger logger = Logger.getLogger(BitBucketPPREnvironmentContributor.class.getName());


  {
    System.setErr(BitBucketPPRUtils.createLoggingProxyForErrors(System.err));
  }

  @Override
  public void buildEnvironmentFor(Job job, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {
    // NOTHING TO DO HERE
  }

  @Override
  public void buildEnvironmentFor(@Nonnull Run run, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {

    List<Cause> causes = null;

    if (run instanceof MatrixRun) {
      MatrixBuild parent = ((MatrixRun) run).getParentBuild();
      if (parent != null) {
        causes = parent.getCauses();
      }
    } else {
      causes = run.getCauses();
    }

    if (causes == null) {
      return;
    }

    causes.stream().forEach((Cause cause) -> {
      try {
        if (cause instanceof BitBucketPPRPullRequestCause) {
          BitBucketPPRPullRequestCause castedCause = (BitBucketPPRPullRequestCause) cause;
          setEnvVarsForCloudPullRequest(envVars, castedCause.getPullRequestPayLoad(),
              castedCause.getHookEvent());
        } else if (cause instanceof BitBucketPPRPullRequestServerCause) {
          BitBucketPPRPullRequestServerCause castedCause =
              (BitBucketPPRPullRequestServerCause) cause;
          setEnvVarsForServerPullRequest(envVars, castedCause.getPullRequestPayLoad(),
              castedCause.getHookEvent());
        } else if (cause instanceof BitBucketPPRRepositoryCause) {
          BitBucketPPRRepositoryCause castedCause = (BitBucketPPRRepositoryCause) cause;
          setEnvVarsForCloudRepository(envVars, castedCause.getRepositoryPayLoad(),
              castedCause.getHookEvent());
        } else if (cause instanceof BitBucketPPRServerRepositoryCause) {
          BitBucketPPRServerRepositoryCause castedCause = (BitBucketPPRServerRepositoryCause) cause;
          setEnvVarsForServerRepository(envVars, castedCause.getServerRepositoryPayLoad(),
              castedCause.getHookEvent());
        }
      } catch (Exception e) {
        e.printStackTrace();
        logger.warning(String.format("Cannot build environment variables for cause %s %s.",
            cause.getShortDescription(), e.toString()));
      }
    });
  }

  private static void setEnvVarsForServerRepository(EnvVars envVars,
      BitBucketPPRServerRepositoryAction action, String hookEvent) {

    String targetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_TARGET_BRANCH, targetBranch);
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, targetBranch);

    String repoName = action.getRepositoryName();
    putEnvVar(envVars, REPOSITORY_NAME, repoName);

    String actor = action.getUser();
    putEnvVar(envVars, BITBUCKET_ACTOR, actor);

    String repositoryId = action.getRepositoryId();
    putEnvVar(envVars, BITBUCKET_REPOSITORY_ID, repositoryId);

    putEnvVar(envVars, BITBUCKET_X_EVENT, hookEvent);

    String payload = action.getPayload().toString();
    putEnvVar(envVars, BITBUCKET_PAYLOAD, payload);
  }

  private static void setEnvVarsForCloudRepository(EnvVars envVars,
      BitBucketPPRRepositoryAction action, String hookEvent) {

    String urlBranchDeprecated = action.getRepositoryUrl();
    putEnvVar(envVars, REPOSITORY_LINK, urlBranchDeprecated);

    String targetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_TARGET_BRANCH, targetBranch);
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, targetBranch);

    String urlBranch = action.getRepositoryUrl();
    putEnvVar(envVars, BITBUCKET_REPOSITORY_URL, urlBranch);

    String repositoryUuid = action.getRepositoryId();
    putEnvVar(envVars, BITBUCKET_REPOSITORY_UUID, repositoryUuid);
    putEnvVar(envVars, BITBUCKET_REPOSITORY_ID, repositoryUuid);

    String actor = action.getUser();
    putEnvVar(envVars, BITBUCKET_ACTOR, actor);

    putEnvVar(envVars, BITBUCKET_X_EVENT, hookEvent);

    String payload = action.getPayload().toString();
    putEnvVar(envVars, BITBUCKET_PAYLOAD, payload);
  }

  private static void setEnvVarsForCloudPullRequest(EnvVars envVars,
      BitBucketPPRPullRequestAction action, String hookEvent) {

    String pullRequestGetLatestCommitFromSourceBranch = action.getLatestCommitFromRef();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_SOURCE_BRANCH,
        pullRequestGetLatestCommitFromSourceBranch);

    String pullRequestGetLatestCommitFromTargetBranch = action.getLatestCommitToRef();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_TARGET_BRANCH,
        pullRequestGetLatestCommitFromTargetBranch);

    String pullRequestSourceBranch = action.getSourceBranch();
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, pullRequestSourceBranch);

    String pullRequestTargetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_TARGET_BRANCH, pullRequestTargetBranch);

    String pullRequestUrlBranch = action.getPullRequestUrl();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LINK, pullRequestUrlBranch);

    String pullRequestId = action.getPullRequestId();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_ID, pullRequestId);

    String actor = action.getUser();
    putEnvVar(envVars, BITBUCKET_ACTOR, actor);

    String title = action.getTitle();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_TITLE, title);

    String description = action.getDescription();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_DESCRIPTION, description);

    putEnvVar(envVars, BITBUCKET_X_EVENT, hookEvent);

    String payload = action.getPayload().toString();
    putEnvVar(envVars, BITBUCKET_PAYLOAD, payload);
  }

  private static void setEnvVarsForServerPullRequest(EnvVars envVars,
      BitBucketPPRPullRequestServerAction action, String hookEvent) {

    String pullRequestGetLatestCommitFromSourceBranch = action.getLatestCommitFromRef();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_SOURCE_BRANCH,
        pullRequestGetLatestCommitFromSourceBranch);

    String pullRequestGetLatestCommitFromTargetBranch = action.getLatestCommitToRef();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LATEST_COMMIT_FROM_TARGET_BRANCH,
        pullRequestGetLatestCommitFromTargetBranch);

    String pullRequestSourceBranch = action.getSourceBranch();
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, pullRequestSourceBranch);

    String pullRequestTargetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_TARGET_BRANCH, pullRequestTargetBranch);

    String pullRequestUrlBranch = action.getPullRequestUrl();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LINK, pullRequestUrlBranch);

    String pullRequestId = action.getPullRequestId();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_ID, pullRequestId);

    String actor = action.getUser();
    putEnvVar(envVars, BITBUCKET_ACTOR, actor);

    String title = action.getTitle();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_TITLE, title);

    String description = action.getDescription();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_DESCRIPTION, description);

    String payload = action.getPayload().toString();
    putEnvVar(envVars, BITBUCKET_PAYLOAD, payload);

    putEnvVar(envVars, BITBUCKET_X_EVENT, hookEvent);

    String pullRequestCommentText = action.getServerComment();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_COMMENT_TEXT, pullRequestCommentText);
  }

  private static void putEnvVar(EnvVars envs, String name, String value) {
    envs.put(name, (value == null ? "" : value));
  }
}
