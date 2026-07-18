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
package io.jenkins.plugins.bitbucketpushandpullrequest.client;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.NotImplementedException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.github.scribejava.core.model.Verb;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRApiResponse;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRNotificationSender;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRUtils;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;

/**
 * Sends build-status notifications to Bitbucket. The API consumer is chosen from the Bitbucket
 * flavor and the configured credential type: username/password credentials use Basic auth on both
 * flavors; a Secret-text credential is an OAuth2 client on Cloud and a Bearer token on Server.
 * This is the single dispatch point for every notification, so cross-cutting policies (like the
 * non-https warning) live here once.
 */
public class DefaultBitBucketPPRClient implements BitBucketPPRClient {
  private static final Logger logger = Logger.getLogger(DefaultBitBucketPPRClient.class.getName());

  private final BitBucketPPRClientType type;
  private final BitBucketPPREventContext context;

  public DefaultBitBucketPPRClient(BitBucketPPRClientType type, BitBucketPPREventContext context) {
    // Fail fast: a null flavor would otherwise silently route Secret-text credentials to the
    // Server/Bearer consumer through the type == CLOUD comparison below.
    this.type = Objects.requireNonNull(type, "client type");
    this.context = context;
  }

  @Override
  public void send(String url, String payload) throws Exception {
    send(Verb.POST, url, payload);
  }

  @Override
  @Deprecated
  public void accept(BitBucketPPRClientVisitor visitor) {
    // The dispatch on the credential type is internal; the visitor wiring only ever applied to
    // the legacy client implementations kept for compatibility.
  }

  @Override
  public void send(Verb verb, String url, String payload) throws Exception {
    BitBucketPPRUtils.warnIfNotHttps(url);
    StandardCredentials credentials = context.getStandardCredentials();

    try {
      BitBucketPPRApiResponse response = switch (credentials) {
        case StandardUsernamePasswordCredentials usernamePasswordCredentials ->
            BitBucketPPRNotificationSender.sendBasicAuth(usernamePasswordCredentials, verb, url,
                payload);
        case StringCredentials stringCredentials -> type == BitBucketPPRClientType.CLOUD
            ? BitBucketPPRNotificationSender.sendOAuth2(stringCredentials, verb, url, payload)
            : BitBucketPPRNotificationSender.sendBearer(stringCredentials, verb, url, payload);
        case null, default -> throw new NotImplementedException(
            "Credentials provider for state notification not found");
      };

      BitBucketPPRUtils.warnOnHttpError(url, response.statusCode(), response.body());

      logger.log(Level.FINEST, "Result of the status notification is: {0}, with status code: {1}",
          new Object[] {response.body(), response.statusCode()});
    } catch (InterruptedException e) {
      // The broad handler upstream logs and swallows: the thread must keep its interrupted
      // status.
      Thread.currentThread().interrupt();
      throw e;
    } catch (ExecutionException | IOException e) {
      logger.log(Level.WARNING, "Error during state notification", e);
    }
  }
}
