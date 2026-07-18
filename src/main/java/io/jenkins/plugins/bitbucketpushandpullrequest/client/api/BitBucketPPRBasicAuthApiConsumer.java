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
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.github.scribejava.core.model.Verb;

public class BitBucketPPRBasicAuthApiConsumer {

  private static final Logger logger =
      Logger.getLogger(BitBucketPPRBasicAuthApiConsumer.class.getName());

  public BitBucketPPRApiResponse send(StandardUsernamePasswordCredentials credentials, Verb verb,
      String url, String payload) throws IOException, NoSuchMethodException {
    logger.finest("Send state notification with StandardUsernamePasswordCredentials");

    // Basic auth is preemptive only: the header built here is the single vehicle for the
    // credentials (see BitBucketPPRApiRequest for the shared transport policy).
    return BitBucketPPRApiRequest.send(
        getAuthHeader(credentials.getUsername(), credentials.getPassword().getPlainText()), verb,
        url, payload);
  }

  private String getAuthHeader(String username, String password) {
    final String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
    return "Basic ".concat(new String(encodedAuth, StandardCharsets.ISO_8859_1));
  }
}
