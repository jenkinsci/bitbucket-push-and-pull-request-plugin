package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

public class BitBucketPPRBasicAuthApiConsumer {

  private static final Logger logger = Logger.getLogger(BitBucketPPRBasicAuthApiConsumer.class.getName());

  public HttpResponse send(StandardUsernamePasswordCredentials credentials, String url,
      String payload) throws IOException {
    logger.finest("Send state notification with StandardUsernamePasswordCredentials");

    final UsernamePasswordCredentials httpAuthCredentials = new UsernamePasswordCredentials(
        credentials.getUsername(), credentials.getPassword().getPlainText());

    final String authHeader =
        getAuthHeader(credentials.getUsername(), credentials.getPassword().getPlainText());

    final org.apache.http.client.CredentialsProvider provider = new BasicCredentialsProvider();
    provider.setCredentials(AuthScope.ANY, httpAuthCredentials);

    final HttpClient client =
        HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

    final HttpPost request = new HttpPost(url);
    request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
    request.setHeader("X-Atlassian-Token", "nocheck");
    request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

    return client.execute(request);
  }

  private String getAuthHeader(String username, String password) {
    final String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
    return "Basic ".concat(new String(encodedAuth, StandardCharsets.ISO_8859_1));
  }
}
