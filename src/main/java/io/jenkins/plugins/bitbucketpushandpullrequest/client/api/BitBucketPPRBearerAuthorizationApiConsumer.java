/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2025, Christian Del Monte.
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
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.github.scribejava.core.model.Verb;

public class BitBucketPPRBearerAuthorizationApiConsumer {

  private static final Logger logger =
      Logger.getLogger(BitBucketPPRBearerAuthorizationApiConsumer.class.getName());

  public BitBucketPPRApiResponse send(StringCredentials credentials, Verb verb, String url,
      String payload) throws IOException, NoSuchMethodException {
    logger.finest("Use BB Bearer Authorization for state notification");

    String authHeader = "Bearer ".concat(credentials.getSecret().getPlainText());

    // Build the client from the JSSE configuration of the Jenkins JVM: createSystem() honours
    // the system properties (javax.net.ssl.trustStore and friends, proxy settings), so
    // certificates are validated against the JVM-configured JSSE trust store and hostnames are
    // verified. A Bitbucket instance using a private CA or a self-signed certificate must have
    // that certificate present in the trust store configured for the Jenkins JVM. The Bearer
    // token travels in the Authorization header, so the connection it travels over must be
    // authenticated -- it must never accept arbitrary certificates.
    try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
      if (verb == Verb.POST) {
        final HttpPost request = new HttpPost(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        request.setHeader("X-Atlassian-Token", "nocheck");

        if (StringUtils.isNotBlank(payload)) {
          request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        }
        return httpClient.execute(request, BitBucketPPRApiResponse::from);
      }

      if (verb == Verb.DELETE) {
        final HttpDelete request = new HttpDelete(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        request.setHeader("X-Atlassian-Token", "nocheck");
        return httpClient.execute(request, BitBucketPPRApiResponse::from);
      }

      throw new NoSuchMethodException();
    }
  }
}
