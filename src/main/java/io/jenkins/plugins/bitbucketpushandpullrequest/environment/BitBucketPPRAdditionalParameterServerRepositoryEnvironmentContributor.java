package io.jenkins.plugins.bitbucketpushandpullrequest.environment;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.google.gson.Gson;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;


@Extension
public class BitBucketPPRAdditionalParameterServerRepositoryEnvironmentContributor  extends EnvironmentContributor {
  private static final Logger LOGGER = Logger
      .getLogger(BitBucketPPRAdditionalParameterServerRepositoryEnvironmentContributor.class.getName());

  Gson gson = new Gson();

  @Override
  public void buildEnvironmentFor(@Nonnull Run run, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {
    
    LOGGER.log(Level.INFO, "Injecting env vars because of server push cause.");
    
    BitBucketPPRServerRepositoryCause cause =
        (BitBucketPPRServerRepositoryCause) run.getCause(BitBucketPPRServerRepositoryCause.class);
    if (cause == null) {
      LOGGER.log(Level.WARNING, "Problem injecting env variables: Cause = null");
      return;
    }

    String urlBranch = cause.getServerRepositoryPayLoad().getRepositoryName();
    putEnvVar(envVars, "REPOSITORY_NAME", urlBranch);
    LOGGER.log(Level.FINEST, "Injecting SERVER_REPOSITORY_NAME: {0}", urlBranch);

    String payloadInString = gson.toJson(cause.getServerRepositoryPayLoad());
    putEnvVar(envVars, "BITBUCKET_PAYLOAD", payloadInString);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOAD: {0}", payloadInString);
  }

  private static void putEnvVar(EnvVars envs, String name, String value) {
    envs.put(name, getString(value, ""));
  }

  private static String getString(String actual, String d) {
    return actual == null ? d : actual;
  }
}