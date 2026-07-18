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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.NotImplementedException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.github.scribejava.core.model.Verb;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRApiResponse;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRBasicAuthApiConsumer;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRBearerAuthorizationApiConsumer;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRUtils;

/**
 * @deprecated kept for binary compatibility with releases up to 4.0.0: the dispatch on the
 *             credential type now lives in {@link DefaultBitBucketPPRClient}. Scheduled for
 *             removal in the next major release.
 */
@Deprecated
public class BitBucketPPRClientServerVisitor implements BitBucketPPRClientVisitor {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPRClientServerVisitor.class.getName());

  @Override
  public void send(StandardCredentials credentials, String url, String payload)
      throws NoSuchMethodException {
    send(credentials, Verb.POST, url, payload);
  }

  @Override
  public void send(StandardCredentials credentials, Verb verb, String url, String payload)
      throws NoSuchMethodException {
    BitBucketPPRUtils.warnIfNotHttps(url);

    if (credentials instanceof StandardUsernamePasswordCredentials usernamePasswordCredentials)
      try {
        BitBucketPPRApiResponse response =
            this.send(usernamePasswordCredentials, verb, url, payload);
        logger.log(Level.FINEST, "Result of the status notification is: {0}, with status code: {1}",
            new Object[] {response.body(), response.statusCode()});
      } catch (IOException e) {
        logger.log(Level.WARNING, "Error during state notification: {0} ", e.getMessage());
      }
    else if (credentials instanceof StringCredentials stringCredentials)
      try {
        BitBucketPPRApiResponse response = this.send(stringCredentials, verb, url, payload);
        logger.log(Level.FINEST, "Result of the state notification is: {0}, with status code: {1}",
            new Object[] {response.body(), response.statusCode()});
      } catch (IOException e) {
        logger.log(Level.WARNING, "Error during state notification: {0} ", e.getMessage());
      }
    else
      throw new NotImplementedException("Credentials provider for state notification not found");
  }

  private BitBucketPPRApiResponse send(StandardUsernamePasswordCredentials credentials, Verb verb,
      String url, String payload) throws IOException, NoSuchMethodException {
    BitBucketPPRBasicAuthApiConsumer api = new BitBucketPPRBasicAuthApiConsumer();
    return api.send(credentials, verb, url, payload);
  }

  private BitBucketPPRApiResponse send(StringCredentials credentials, Verb verb, String url,
      String payload) throws IOException, NoSuchMethodException {
    logger.finest("Set BB StringCredentials for BB Server state notification");

    BitBucketPPRBearerAuthorizationApiConsumer api =
        new BitBucketPPRBearerAuthorizationApiConsumer();
    return api.send(credentials, verb, url, payload);
  }
}
