package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

public class BitBucketPPROAuth2Api {

  public Response sendRequest(String url, String payload, StringCredentials credentials) {
    try {
      OAuth20Service service = new ServiceBuilder(credentials.getId())
          .apiSecret(credentials.getSecret().getPlainText()).build(BitbucketApi.instance());
      
      OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrant();
      OAuthRequest request = new OAuthRequest(Verb.POST, url);
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      // request.addPayload(MAPPER.writeValueAsString(payload));
      request.setPayload(payload);
      service.signRequest(token, request);
      
      return service.execute(request);

    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }
}
