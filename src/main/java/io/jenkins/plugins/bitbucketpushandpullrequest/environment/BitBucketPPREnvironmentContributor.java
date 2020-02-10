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

  private static final String BITBUCKET_PULL_REQUEST_ID = "BITBUCKET_PULL_REQUEST_ID";
  private static final String BITBUCKET_PULL_REQUEST_LINK = "BITBUCKET_PULL_REQUEST_LINK";
  private static final String BITBUCKET_TARGET_BRANCH = "BITBUCKET_TARGET_BRANCH";
  private static final String BITBUCKET_REPOSITORY_UUID = "BITBUCKET_REPOSITORY_UUID";
  private static final String BITBUCKET_REPOSITORY_URL = "BITBUCKET_REPOSITORY_URL";
  private static final String BITBUCKET_SOURCE_BRANCH = "BITBUCKET_SOURCE_BRANCH";
  private static final String REPOSITORY_LINK = "REPOSITORY_LINK";
  private static final String REPOSITORY_NAME = "REPOSITORY_NAME";
  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPREnvironmentContributor.class.getName());

  @Override
  public void buildEnvironmentFor(@Nonnull Run run, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {

    LOGGER.log(Level.INFO, "Injecting env vars because of pull request cause.");

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

  private void setEnvVarsForServerRepository(EnvVars envVars,
      BitBucketPPRServerRepositoryAction action) {
    String repoName = action.getRepositoryName();
    putEnvVar(envVars, REPOSITORY_NAME, repoName);
  }

  private void setEnvVarsForCloudRepository(EnvVars envVars, BitBucketPPRRepositoryAction action) {
    String urlBranchDeprecated = action.getRepositoryUrl();
    putEnvVar(envVars, REPOSITORY_LINK, urlBranchDeprecated);
    LOGGER.log(Level.FINEST, "Injecting REPOSOTORY_LINK: {0}", urlBranchDeprecated);

    String targetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, targetBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", targetBranch);

    String urlBranch = action.getRepositoryUrl();
    putEnvVar(envVars, BITBUCKET_REPOSITORY_URL, urlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_REPOSITORY_URL: {0}", urlBranch);

    String repositoryUuid = action.getRepositoryUuid();
    putEnvVar(envVars, BITBUCKET_REPOSITORY_UUID, repositoryUuid);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PUSH_REPOSITORY_UUID: {0}", repositoryUuid);
  }

  private void setEnvVarsForCloudPullRequest(EnvVars envVars,
      BitBucketPPRPullRequestAction action) {
    String pullRequestSourceBranch = action.getSourceBranch();
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, pullRequestSourceBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", pullRequestSourceBranch);

    String pullRequestTargetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_TARGET_BRANCH, pullRequestTargetBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_TARGET_BRANCH: {0}", pullRequestTargetBranch);

    String pullRequestUrlBranch = action.getPullRequestUrl();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LINK, pullRequestUrlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_LINK: {0}", pullRequestUrlBranch);

    String pullRequestId = action.getPullRequestId();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_ID, pullRequestId);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_ID: {0}", pullRequestId);
  }

  private void setEnvVarsForServerPullRequest(EnvVars envVars,
      BitBucketPPRPullRequestServerAction action) {
    String pullRequestSourceBranch = action.getSourceBranch();
    putEnvVar(envVars, BITBUCKET_SOURCE_BRANCH, pullRequestSourceBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", pullRequestSourceBranch);

    String pullRequestTargetBranch = action.getTargetBranch();
    putEnvVar(envVars, BITBUCKET_TARGET_BRANCH, pullRequestTargetBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_TARGET_BRANCH: {0}", pullRequestTargetBranch);

    String pullRequestUrlBranch = action.getPullRequestUrl();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_LINK, pullRequestUrlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_LINK: {0}", pullRequestUrlBranch);

    String pullRequestId = action.getPullRequestId();
    putEnvVar(envVars, BITBUCKET_PULL_REQUEST_ID, pullRequestId);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_ID: {0}", pullRequestId);
  }

  private static void putEnvVar(EnvVars envs, String name, String value) {
    envs.put(name, getString(value, ""));
  }

  private static String getString(String actual, String d) {
    return actual == null ? d : actual;
  }
}
