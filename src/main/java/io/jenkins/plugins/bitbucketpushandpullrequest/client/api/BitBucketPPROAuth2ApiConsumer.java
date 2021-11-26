package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

public class BitBucketPPROAuth2ApiConsumer {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPROAuth2ApiConsumer.class.getName());

  public Response send(StringCredentials credentials, Verb verb, String url, String payload)
      throws InterruptedException, ExecutionException, IOException {
    logger.finest("Set BB StringCredentials for BB Cloud state notification");

    OAuth20Service service = new ServiceBuilder(credentials.getId())
        .apiSecret(credentials.getSecret().getPlainText()).build(BitbucketPPROAuth2Api.instance());

    OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrant();
    OAuthRequest request = new OAuthRequest(verb, url);
    request.addHeader("Content-Type", "application/json;charset=UTF-8");

    if (verb == Verb.POST) {
      request.setPayload(payload);
    }

    service.signRequest(token, request);

    return service.execute(request);
  }
}
