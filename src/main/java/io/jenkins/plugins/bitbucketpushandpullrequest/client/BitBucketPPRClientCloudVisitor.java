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
package io.jenkins.plugins.bitbucketpushandpullrequest.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRBasicAuthApiConsumer;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPROAuth2ApiConsumer;

public class BitBucketPPRClientCloudVisitor implements BitBucketPPRClientVisitor {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRClientCloudVisitor.class.getName());

  @Override
  public void send(StandardCredentials credentials, String url, String payload)
      throws NoSuchMethodException, InterruptedException {
    send(credentials, Verb.POST, url, payload);
  }

  @Override
  public void send(StandardCredentials credentials, Verb verb, String url, String payload)
      throws InterruptedException, NoSuchMethodException {
    if (credentials instanceof StandardUsernamePasswordCredentials usernamePasswordCredentials)
      try {
        final HttpResponse response =
            this.send(usernamePasswordCredentials, verb, url, payload);
        HttpEntity responseEntity = response.getEntity();
        final String responseBody =
            responseEntity == null ? "empty" : EntityUtils.toString(responseEntity);

        logger.log(Level.FINEST, "Result of the status notification is: {0}, with status code: {1}",
            new Object[] {responseBody, response.getStatusLine().getStatusCode()});
      } catch (IOException e) {
        logger.log(Level.WARNING, "Error during state notification: {0} ", e.getMessage());
      }
    else if (credentials instanceof StringCredentials stringCredentials) {
      try {
        Response response = this.send(stringCredentials, verb, url, payload);

        logger.log(Level.FINEST, "Result of the state notification is: {0}, with status code: {1}",
            new Object[] {response.getBody(), response.getCode()});
      } catch (ExecutionException | IOException e) {
        logger.log(Level.WARNING, "Error during state notification: {0} ", e.getMessage());
      }
    } else
      throw new NotImplementedException("Credentials provider for state notification not found");
  }

  private HttpResponse send(StandardUsernamePasswordCredentials credentials, Verb verb, String url,
      String payload) throws IOException, NoSuchMethodException {

    BitBucketPPRBasicAuthApiConsumer api = new BitBucketPPRBasicAuthApiConsumer();
    return api.send(credentials, verb, url, payload);
  }

  private Response send(StringCredentials credentials, Verb verb, String url, String payload)
      throws InterruptedException, ExecutionException, IOException {
    logger.finest("Set BB StringCredentials for BB Cloud state notification");

    BitBucketPPROAuth2ApiConsumer api = new BitBucketPPROAuth2ApiConsumer();
    return api.send(credentials, verb, url, payload);
  }

}
