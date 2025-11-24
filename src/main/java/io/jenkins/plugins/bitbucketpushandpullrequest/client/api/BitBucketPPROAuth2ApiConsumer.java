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
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

public class BitBucketPPROAuth2ApiConsumer {
  private static final Logger logger =
      Logger.getLogger(BitBucketPPROAuth2ApiConsumer.class.getName());

  public Response send(StringCredentials credentials, Verb verb, String url, String payload)
      throws InterruptedException, ExecutionException, IOException {
    logger.finest("Set BB StringCredentials for BB Cloud state notification");

    OAuth20Service service = new ServiceBuilder(credentials.getId())
        .apiSecret(credentials.getSecret().getPlainText()).build(BitbucketPPROAuth2Api.instance());

    OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrant();
    OAuthRequest request = new OAuthRequest(verb, url);
    request.addHeader("Content-Type", "application/json;charset=UTF-8");


    if (verb == Verb.POST && StringUtils.isNotBlank(payload)) {
      request.setPayload(payload);
    }

    service.signRequest(token, request);

    return service.execute(request);
  }
}
