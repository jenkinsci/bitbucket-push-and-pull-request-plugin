package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class BitbucketApi extends DefaultApi20 {

  public BitbucketApi() { }

  private static class InstanceHolder {
    private static final BitbucketApi INSTANCE = new BitbucketApi();
  }

  public static BitbucketApi instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "https://bitbucket.org/site/oauth2/access_token";
  }

  @Override
  protected String getAuthorizationBaseUrl() {
    return "https://bitbucket.org/site/oauth2/authorize";
  }
}
