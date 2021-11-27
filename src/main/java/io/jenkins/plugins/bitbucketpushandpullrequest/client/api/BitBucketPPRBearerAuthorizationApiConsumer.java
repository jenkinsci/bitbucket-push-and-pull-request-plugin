package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.github.scribejava.core.model.Verb;

public class BitBucketPPRBearerAuthorizationApiConsumer {

  private static final Logger logger =
      Logger.getLogger(BitBucketPPRBearerAuthorizationApiConsumer.class.getName());

  public HttpResponse send(StringCredentials credentials, Verb verb, String url, String payload)
      throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
      NoSuchMethodException {
    logger.finest("Use BB Bearer Authorization for state notification");

    TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
    SSLContext sslContext =
        SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
    SSLConnectionSocketFactory sslsf =
        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

    Registry<ConnectionSocketFactory> socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf)
            .register("http", new PlainConnectionSocketFactory()).build();

    BasicHttpClientConnectionManager connectionManager =
        new BasicHttpClientConnectionManager(socketFactoryRegistry);
    CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
        .setConnectionManager(connectionManager).build();

    String authHeader = "Bearer ".concat(credentials.getSecret().getPlainText());

    if (verb == Verb.POST) {
      final HttpPost request = new HttpPost(url);
      request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
      request.setHeader("X-Atlassian-Token", "nocheck");

      if (!payload.isEmpty())
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

      request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
      return httpClient.execute(request);
    }

    if (verb == Verb.DELETE) {
      final HttpDelete request = new HttpDelete(url);
      request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
      request.setHeader("X-Atlassian-Token", "nocheck");
      return httpClient.execute(request);
    }

    throw new NoSuchMethodException();
  }
}
