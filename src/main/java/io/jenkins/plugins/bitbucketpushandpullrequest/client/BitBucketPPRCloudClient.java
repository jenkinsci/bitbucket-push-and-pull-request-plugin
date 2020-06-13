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
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
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
import hudson.model.Run;
import hudson.plugins.git.UserRemoteConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;


public class BitBucketPPRCloudClient implements BitBucketPPRClient {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRTrigger.class.getName());
  private final UserRemoteConfig config;
  private Run<?, ?> run;

  public BitBucketPPRCloudClient(final UserRemoteConfig config, Run<?, ?> run) {
    this.run = run;
    this.config = config;
  }

  public void sendWithUsernamePasswordCredentials(final String url, String payload) {
    LOGGER.info("Set BB Credentials");
    final org.apache.http.client.CredentialsProvider provider = new BasicCredentialsProvider();
    final UsernamePasswordCredentials credentials =
        new UsernamePasswordCredentials(getStandardUsernamePasswordCredentials().getUsername(),
            getStandardUsernamePasswordCredentials().getPassword().getPlainText());
    provider.setCredentials(AuthScope.ANY, credentials);

    final String authHeader = createPreemptiveHeadersForDefautlCredentialsProvider();

    final HttpClient client =
        HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

    final HttpPost request = new HttpPost(url);
    request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

    request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

    try {
      final HttpResponse response = client.execute(request);
      final int statusCode = response.getStatusLine().getStatusCode();
      final String result = EntityUtils.toString(response.getEntity());
      LOGGER.info("Result is: " + result + " Status Code: " + statusCode);
    } catch (final Exception e) {
      LOGGER.warning("Error: " + e.getMessage());
    }
  }

  private String createPreemptiveHeadersForDefautlCredentialsProvider() {
    final String auth = getStandardUsernamePasswordCredentials().getUsername() + ":"
        + getStandardUsernamePasswordCredentials().getPassword().getPlainText();

    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
    String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

    return authHeader;
  }

  private StandardUsernamePasswordCredentials getStandardUsernamePasswordCredentials() {
    String credentialsId = config.getCredentialsId();
    String url = config.getUrl();

    if (credentialsId == null) {
      LOGGER.info("Credentials ID not found");
      return null;
    }

    if (url == null) {
      LOGGER.info("Cannot find Url in config");
      return null;
    }

    return CredentialsProvider.findCredentialById(credentialsId,
        StandardUsernamePasswordCredentials.class, run,
        URIRequirementBuilder.fromUri(url).build());
  }
}
