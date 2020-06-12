package io.jenkins.plugins.bitbucketpushandpullrequest.client;

import hudson.model.Run;
import hudson.plugins.git.UserRemoteConfig;

public class BitBucketPPRServerClient implements BitBucketPPRClient {

  public BitBucketPPRServerClient(UserRemoteConfig config, Run<?, ?> run) {}

  @Override
  public void sendWithUsernamePasswordCredentials(String url, String payload) {
    // TODO Auto-generated method stub

  }

}
