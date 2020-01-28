package io.jenkins.plugins.bitbucketpushandpullrequest.environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import com.google.gson.Gson;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.cloud.BitBucketPPRPullRequestCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.server.BitBucketPPRPullRequestServerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;


@Extension
public class BitBucketPPREnvironmentContributor extends EnvironmentContributor {

  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPREnvironmentContributor.class.getName());

  Gson gson = new Gson();

  @Override
  public void buildEnvironmentFor(@Nonnull Run run, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {

    LOGGER.log(Level.INFO, "Injecting env vars because of pull request cause.");

    List<Cause> causes = new ArrayList<>();
    for (Object c : run.getCauses()) {
      causes.add((Cause) c);
    }

    for (Cause cause : causes) {
      if (cause instanceof BitBucketPPRPullRequestCause) {
        setEnvVarsForCloudPullRequest(envVars, (BitBucketPPRPullRequestCause) cause);
        continue;
      }
      if (cause instanceof BitBucketPPRPullRequestServerCause) {
        setEnvVarsForServerPullRequest(envVars, (BitBucketPPRPullRequestServerCause) cause);
        continue;
      }
      if (cause instanceof BitBucketPPRRepositoryCause) {
        try {
          setEnvVarsForCloudRepository(envVars, (BitBucketPPRRepositoryCause) cause);
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Something didn't work: {0}", e.getMessage());
        }
        continue;
      }
      if (cause instanceof BitBucketPPRServerRepositoryCause) {
        setEnvVarsForServerRepository(envVars, (BitBucketPPRServerRepositoryCause) cause);
      }
    }
  }

  private void setEnvVarsForServerRepository(EnvVars envVars,
      BitBucketPPRServerRepositoryCause cause) {
    String repoName = cause.getServerRepositoryPayLoad().getRepositoryName();
    putEnvVar(envVars, "REPOSITORY_NAME", repoName);
  }

  private void setEnvVarsForCloudRepository(EnvVars envVars, BitBucketPPRRepositoryCause cause) {
    String urlBranchDeprecated = cause.getRepositoryPayLoad().getRepositoryUrl();
    putEnvVar(envVars, "REPOSITORY_LINK", urlBranchDeprecated);
    LOGGER.log(Level.FINEST, "Injecting REPOSOTORY_LINK: {0}", urlBranchDeprecated);

    String targetBranch = cause.getRepositoryPayLoad().getTargetBranch();
    putEnvVar(envVars, "BITBUCKET_SOURCE_BRANCH", targetBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", targetBranch);

    String urlBranch = cause.getRepositoryPayLoad().getRepositoryUrl();
    putEnvVar(envVars, "BITBUCKET_REPOSITORY_URL", urlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_REPOSITORY_URL: {0}", urlBranch);

    String repositoryUuid = cause.getRepositoryPayLoad().getRepositoryUuid();
    putEnvVar(envVars, "BITBUCKET_REPOSITORY_UUID", repositoryUuid);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PUSH_REPOSITORY_UUID: {0}", repositoryUuid);
  }

  private void setEnvVarsForCloudPullRequest(EnvVars envVars, BitBucketPPRPullRequestCause cause) {
    String pullRequestSourceBranch = cause.getPullRequestPayLoad().getSourceBranch();
    putEnvVar(envVars, "BITBUCKET_SOURCE_BRANCH", pullRequestSourceBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", pullRequestSourceBranch);

    String pullRequestTargetBranch = cause.getPullRequestPayLoad().getTargetBranch();
    putEnvVar(envVars, "BITBUCKET_TARGET_BRANCH", pullRequestTargetBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_TARGET_BRANCH: {0}", pullRequestTargetBranch);

    String pullRequestUrlBranch = cause.getPullRequestPayLoad().getPullRequestUrl();
    putEnvVar(envVars, "BITBUCKET_PULL_REQUEST_LINK", pullRequestUrlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_LINK: {0}", pullRequestUrlBranch);

    String pullRequestId = cause.getPullRequestPayLoad().getPullRequestId();
    putEnvVar(envVars, "BITBUCKET_PULL_REQUEST_ID", pullRequestId);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_ID: {0}", pullRequestId);
  }

  private void setEnvVarsForServerPullRequest(EnvVars envVars,
      BitBucketPPRPullRequestServerCause cause) {
    String pullRequestSourceBranch = cause.getPullRequestPayLoad().getSourceBranch();
    putEnvVar(envVars, "BITBUCKET_SOURCE_BRANCH", pullRequestSourceBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", pullRequestSourceBranch);

    String pullRequestTargetBranch = cause.getPullRequestPayLoad().getTargetBranch();
    putEnvVar(envVars, "BITBUCKET_TARGET_BRANCH", pullRequestTargetBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_TARGET_BRANCH: {0}", pullRequestTargetBranch);

    String pullRequestUrlBranch = cause.getPullRequestPayLoad().getPullRequestUrl();
    putEnvVar(envVars, "BITBUCKET_PULL_REQUEST_LINK", pullRequestUrlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_LINK: {0}", pullRequestUrlBranch);

    String pullRequestId = cause.getPullRequestPayLoad().getPullRequestId();
    putEnvVar(envVars, "BITBUCKET_PULL_REQUEST_ID", pullRequestId);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PULL_REQUEST_ID: {0}", pullRequestId);
  }

  private static void putEnvVar(EnvVars envs, String name, String value) {
    envs.put(name, getString(value, ""));
  }

  private static String getString(String actual, String d) {
    return actual == null ? d : actual;
  }
}
