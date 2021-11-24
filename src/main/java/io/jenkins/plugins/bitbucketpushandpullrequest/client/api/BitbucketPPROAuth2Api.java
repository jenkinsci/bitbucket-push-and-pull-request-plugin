package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class BitbucketPPROAuth2Api extends DefaultApi20 {

  public BitbucketPPROAuth2Api() { }

  private static class InstanceHolder {
    private static final BitbucketPPROAuth2Api INSTANCE = new BitbucketPPROAuth2Api();
  }

  public static BitbucketPPROAuth2Api instance() {
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
