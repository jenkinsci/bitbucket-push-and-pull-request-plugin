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

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpEntity;
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

public class BitBucketPPRClientCloudVisitor implements BitBucketPPRClientVisitor {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRClientCloudVisitor.class.getName());

  @Override
  public void send(StandardCredentials standardCredentials, String url, String payload) {
    if (standardCredentials instanceof StandardUsernamePasswordCredentials)
      this.send((StandardUsernamePasswordCredentials) standardCredentials, url, payload);
    else if (standardCredentials instanceof SSHUserPrivateKey) {
      this.send((SSHUserPrivateKey) standardCredentials, url, payload);
    } else
      throw new NotImplementedException("Credentials provider for back propagation not found");
  }

  private void send(StandardUsernamePasswordCredentials standardCredentials, String url,
      String payload) {
    logger.info("Set BB StandardUsernamePasswordCredentials for BB Cloud backpropagation");

    final org.apache.http.client.CredentialsProvider provider = new BasicCredentialsProvider();
    String username = standardCredentials.getUsername();
    String password = standardCredentials.getPassword().getPlainText();

    try {
      final UsernamePasswordCredentials credentials =
          new UsernamePasswordCredentials(username, password);
      provider.setCredentials(AuthScope.ANY, credentials);
      final String auth = username + ":" + password;
      byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
      String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

      final HttpClient client =
          HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
      final HttpPost request = new HttpPost(url);
      request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
      request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
      final HttpResponse response = client.execute(request);
      final int statusCode = response.getStatusLine().getStatusCode();
      
      HttpEntity responseEntity = response.getEntity();
      final String result = responseEntity == null ? "" : EntityUtils.toString(responseEntity);
      logger.info("Result is: " + result + " Status Code: " + statusCode);
    } catch (Throwable t) {
      logger.warning("Error during backpropagation to BB Cloud: " + t.getMessage());
    }
  }

  private void send(SSHUserPrivateKey standardCredentials, String url, String payload) {

    throw new NotImplementedException(
        "This authentication method is not supported by the BB Cloud Rest API.");
  }
}
