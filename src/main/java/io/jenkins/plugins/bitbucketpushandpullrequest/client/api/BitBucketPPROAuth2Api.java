package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class BitBucketPPROAuth2Api extends DefaultApi20 {


  public BitBucketPPROAuth2Api()   { 
    /* TODO document why this constructor is empty */ 
  }

  private static class InstanceHolder {
    private static final BitBucketPPROAuth2Api INSTANCE = new BitBucketPPROAuth2Api();
  }

  public static BitBucketPPROAuth2Api instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "https://bitbucket.org/site/oauth2/access_token";
  }

  @Override
  protected String getAuthorizationBaseUrl() {
    return "https://bitbucket.org/site/oauth2/access_token";
  }

}
