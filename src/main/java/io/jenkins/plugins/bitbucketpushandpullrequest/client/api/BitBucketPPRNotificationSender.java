/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2026, Christian Del Monte.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Sends build-status notifications and fully owns the transport lifecycle: clients, services and
 * responses are closed before a {@link BitBucketPPRApiResponse} is returned. This is the internal
 * replacement for the deprecated per-credential consumer classes, which keep their original
 * live-response contract for binary compatibility.
 */
@Restricted(NoExternalUse.class)
public final class BitBucketPPRNotificationSender {

  private BitBucketPPRNotificationSender() {}

  public static BitBucketPPRApiResponse sendBasicAuth(
      StandardUsernamePasswordCredentials credentials, Verb verb, String url, String payload)
      throws IOException, NoSuchMethodException {
    // Basic auth is preemptive only: this header is the single vehicle for the credentials.
    return send(basicAuthHeader(credentials.getUsername(),
        credentials.getPassword().getPlainText()), verb, url, payload);
  }

  public static BitBucketPPRApiResponse sendBearer(StringCredentials credentials, Verb verb,
      String url, String payload) throws IOException, NoSuchMethodException {
    return send("Bearer ".concat(credentials.getSecret().getPlainText()), verb, url, payload);
  }

  public static BitBucketPPRApiResponse sendOAuth2(StringCredentials credentials, Verb verb,
      String url, String payload) throws InterruptedException, ExecutionException, IOException {
    // Service and response are both Closeable (the service wraps its own HTTP client); reading
    // code and body inside the try-with-resources detaches the returned value from the
    // transport, which is fully released on exit.
    try (OAuth20Service service = new ServiceBuilder(credentials.getId())
        .apiSecret(credentials.getSecret().getPlainText())
        .build(BitbucketPPROAuth2Api.instance())) {

      OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrant();
      OAuthRequest request = new OAuthRequest(verb, url);
      request.addHeader("Content-Type", "application/json;charset=UTF-8");

      if (verb == Verb.POST && StringUtils.isNotBlank(payload)) {
        request.setPayload(payload);
      }

      service.signRequest(token, request);

      try (Response response = service.execute(request)) {
        return BitBucketPPRApiResponse.from(response);
      }
    } catch (InterruptedException e) {
      // Restored next to the blocking calls, so every caller of this sender inherits the
      // invariant: the thread keeps its interrupted status.
      Thread.currentThread().interrupt();
      throw e;
    }
  }

  // The Authorization header set here is the only place credentials travel: no credentials
  // provider is registered, so they can never be offered to a proxy answering 407, and the
  // system default provider keeps handling JVM-configured proxy credentials
  // (java.net.Authenticator). createSystem() builds the client from the JSSE configuration of
  // the Jenkins JVM (javax.net.ssl.trustStore and friends, proxy settings): certificates are
  // validated against the JVM trust store and hostnames are verified, so the header never
  // travels over an unauthenticated connection.
  private static BitBucketPPRApiResponse send(String authHeader, Verb verb, String url,
      String payload) throws IOException, NoSuchMethodException {
    if (verb != Verb.POST && verb != Verb.DELETE) {
      throw new NoSuchMethodException();
    }

    RequestBuilder request =
        (verb == Verb.POST ? RequestBuilder.post(url) : RequestBuilder.delete(url))
            .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .addHeader("X-Atlassian-Token", "nocheck");
    if (verb == Verb.POST && StringUtils.isNotBlank(payload)) {
      request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    }

    try (CloseableHttpClient client = HttpClients.createSystem()) {
      return client.execute(request.build(), BitBucketPPRApiResponse::from);
    }
  }

  private static String basicAuthHeader(String username, String password) {
    String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
    return "Basic ".concat(new String(encodedAuth, StandardCharsets.ISO_8859_1));
  }
}
