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
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.github.scribejava.core.model.Verb;

public class BitBucketPPRBearerAuthorizationApiConsumer {

  private static final Logger logger =
      Logger.getLogger(BitBucketPPRBearerAuthorizationApiConsumer.class.getName());

  public BitBucketPPRApiResponse send(StringCredentials credentials, Verb verb, String url,
      String payload) throws IOException, NoSuchMethodException {
    logger.finest("Use BB Bearer Authorization for state notification");

    // The token travels only in the Authorization header, over a connection validated against
    // the JVM trust store (see BitBucketPPRApiRequest for the shared transport policy).
    return BitBucketPPRApiRequest.send("Bearer ".concat(credentials.getSecret().getPlainText()),
        verb, url, payload);
  }
}
