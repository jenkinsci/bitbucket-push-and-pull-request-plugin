/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2020, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.client;

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
import org.apache.http.util.EntityUtils;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRUtils;


public class BitBucketPPRCloudClient implements BitBucketPPRClient {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRCloudClient.class.getName());
  private BitBucketPPREventContext context;

  {
    System.setErr(BitBucketPPRUtils.createLoggingProxy(System.err));
  }

  public BitBucketPPRCloudClient(BitBucketPPREventContext context) {
    this.context = context;
  }

  public void sendWithUsernamePasswordCredentials(final String url, String payload) {
    LOGGER.info("Set BB Credentials");
    final org.apache.http.client.CredentialsProvider provider = new BasicCredentialsProvider();

    String username = context.getStandardUsernamePasswordCredentials().getUsername();
    String password = context.getStandardUsernamePasswordCredentials().getPassword().getPlainText();

    try {
      final UsernamePasswordCredentials credentials =
          new UsernamePasswordCredentials(username, password);
      provider.setCredentials(AuthScope.ANY, credentials);
      final String authHeader = createPreemptiveHeadersForDefautlCredentialsProvider();
      final HttpClient client =
          HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
      final HttpPost request = new HttpPost(url);
      request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
      request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
      final HttpResponse response = client.execute(request);
      final int statusCode = response.getStatusLine().getStatusCode();
      final String result = EntityUtils.toString(response.getEntity());
      LOGGER.info("Result is: " + result + " Status Code: " + statusCode);
    } catch (Throwable t) {
      LOGGER.warning("Error: " + t.getMessage());
    }
  }

  private String createPreemptiveHeadersForDefautlCredentialsProvider() {
    final String auth = context.getStandardUsernamePasswordCredentials().getUsername() + ":"
        + context.getStandardUsernamePasswordCredentials().getPassword().getPlainText();

    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
    String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

    return authHeader;
  }
}
