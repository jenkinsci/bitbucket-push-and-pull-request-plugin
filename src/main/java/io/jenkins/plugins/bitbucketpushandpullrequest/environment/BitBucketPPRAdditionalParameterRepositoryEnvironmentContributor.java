package io.jenkins.plugins.bitbucketpushandpullrequest.environment;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.google.gson.Gson;

import hudson.EnvVars;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRRepositoryCause;


public class BitBucketPPRAdditionalParameterRepositoryEnvironmentContributor
    extends EnvironmentContributor {
  private static final Logger LOGGER = Logger
      .getLogger(BitBucketPPRAdditionalParameterRepositoryEnvironmentContributor.class.getName());

  Gson gson = new Gson();

  @Override
  public void buildEnvironmentFor(@Nonnull Run run, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {

    BitBucketPPRRepositoryCause cause =
        (BitBucketPPRRepositoryCause) run.getCause(BitBucketPPRRepositoryCause.class);
   
    if (cause == null) {
      return;
    }
    
    @Deprecated
    String urlBranchDeprecated = cause.getRepositoryPayLoad().getRepositoryUrl();
    putEnvVar(envVars, "REPOSITORY_LINK", urlBranchDeprecated);
    LOGGER.log(Level.FINEST, "Injecting REPOSOTORY_LINK: {0}", urlBranchDeprecated);

    String payloadInString = gson.toJson(cause.getRepositoryPayLoad());
    putEnvVar(envVars, "BITBUCKET_PAYLOAD", payloadInString);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOAD: {0}", payloadInString);
    
    String sourceBranch = cause.getRepositoryPayLoad().getRepositoryName();
    putEnvVar(envVars, "BITBUCKET_SOURCE_BRANCH", sourceBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", sourceBranch);

    String urlBranch = cause.getRepositoryPayLoad().getRepositoryUrl();
    putEnvVar(envVars, "BITBUCKET_REPOSITORY_URL", urlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_REPOSITORY_URL: {0}", urlBranch);
    
    String repositoryUuid = cause.getRepositoryPayLoad().getRepositoryUuid();
    putEnvVar(envVars, "BITBUCKET_REPOSITORY_UUID", urlBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PUSH_REPOSITORY_UUID: {0}", repositoryUuid);
  }

  private static void putEnvVar(EnvVars envs, String name, String value) {
    envs.put(name, getString(value, ""));
  }

  private static String getString(String actual, String d) {
    return actual == null ? d : actual;
  }
}
