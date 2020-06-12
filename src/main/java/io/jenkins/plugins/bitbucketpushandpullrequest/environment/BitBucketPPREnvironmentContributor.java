package io.jenkins.plugins.bitbucketpushandpullrequest.environment;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.EnvironmentContributor;
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

@Extension
public class BitBucketPPREnvironmentContributor extends EnvironmentContributor {

  static final String BITBUCKET_PULL_REQUEST_ID = "BITBUCKET_PULL_REQUEST_ID";
  static final String BITBUCKET_PULL_REQUEST_LINK = "BITBUCKET_PULL_REQUEST_LINK";
  static final String BITBUCKET_TARGET_BRANCH = "BITBUCKET_TARGET_BRANCH";
  static final String BITBUCKET_REPOSITORY_UUID = "BITBUCKET_REPOSITORY_UUID";
  static final String BITBUCKET_REPOSITORY_ID = "BITBUCKET_REPOSITORY_ID";
  static final String BITBUCKET_REPOSITORY_URL = "BITBUCKET_REPOSITORY_URL";
  static final String BITBUCKET_SOURCE_BRANCH = "BITBUCKET_SOURCE_BRANCH";
  static final String REPOSITORY_LINK = "REPOSITORY_LINK";
  static final String REPOSITORY_NAME = "REPOSITORY_NAME";
  static final String BITBUCKET_ACTOR = "BITBUCKET_ACTOR";
  static final String BITBUCKET_PULL_REQUEST_TITLE = "BITBUCKET_PULL_REQUEST_TITLE";
  static final String BITBUCKET_PULL_REQUEST_DESCRIPTION = "BITBUCKET_PULL_REQUEST_DESCRIPTION";
  static final String BITBUCKET_PAYLOAD = "BITBUCKET_PAYLOAD";

  static final Logger LOGGER = Logger.getLogger(BitBucketPPREnvironmentContributor.class.getName());

  @Override
  public void buildEnvironmentFor(@Nonnull Run run, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {

    LOGGER.log(Level.FINEST, "Injecting env vars because of pull request cause.");

    List<Cause> causes = run.getCauses();

    causes.stream().forEach((Cause cause) -> {
      try {
        if (cause instanceof BitBucketPPRPullRequestCause) {
          BitBucketPPRPullRequestCause castedCause = (BitBucketPPRPullRequestCause) cause;
          setEnvVarsForCloudPullRequest(envVars, castedCause.getPullRequestPayLoad());
        } else if (cause instanceof BitBucketPPRPullRequestServerCause) {
          BitBucketPPRPullRequestServerCause castedCause =
              (BitBucketPPRPullRequestServerCause) cause;
          setEnvVarsForServerPullRequest(envVars, castedCause.getPullRequestPayLoad());
        } else if (cause instanceof BitBucketPPRRepositoryCause) {
          BitBucketPPRRepositoryCause castedCause = (BitBucketPPRRepositoryCause) cause;
          setEnvVarsForCloudRepository(envVars, castedCause.getRepositoryPayLoad());
        } else if (cause instanceof BitBucketPPRServerRepositoryCause) {
          BitBucketPPRServerRepositoryCause castedCause = (BitBucketPPRServerRepositoryCause) cause;
          setEnvVarsForServerRepository(envVars, castedCause.getServerRepositoryPayLoad());
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Something didn't work: {0}", e.getMessage());
      }
    });
  }

  private static void setEnvVarsForServerRepository(EnvVars envVars,
      BitBucketPPRServerRepositoryAction action) {
    LOGGER.log(Level.FINEST, "Injecting env vars for Server Push");

    String targetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_TARGET_BRANCH, targetBranch);
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, targetBranch);

    String repoName = action.getRepositoryName();
    putEnvVar(envVars, REPOSITORY_NAME, repoName);

    String actor = action.getUser();
    putEnvVar(envVars, BITBUCKET_ACTOR, actor);

    String repositoryId = action.getRepositoryId();
    putEnvVar(envVars, BITBUCKET_REPOSITORY_ID, repositoryId);

    String payload = action.getPayload().toString();
    putEnvVar(envVars, BITBUCKET_PAYLOAD, payload);
  }

  private static void setEnvVarsForCloudRepository(EnvVars envVars,
      BitBucketPPRRepositoryAction action) {
    LOGGER.log(Level.FINEST, "Injecting env vars for Cloud Push");

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

    String payload = action.getPayload().toString();
    putEnvVar(envVars, BITBUCKET_PAYLOAD, payload);
  }

  private static void setEnvVarsForCloudPullRequest(EnvVars envVars,
      BitBucketPPRPullRequestAction action) {
    LOGGER.log(Level.FINEST, "Injecting env vars for Cloud PR");

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
  }

  private static void setEnvVarsForServerPullRequest(EnvVars envVars,
      BitBucketPPRPullRequestServerAction action) {
    LOGGER.log(Level.FINEST, "Injecting env vars for Server PR");

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
  }

  private static void putEnvVar(EnvVars envs, String name, String value) {
    envs.put(name, (value == null ? "" : value));
    LOGGER.log(Level.FINEST, String.format("Injecting env var: %s=%s", name, value));
  }
}
