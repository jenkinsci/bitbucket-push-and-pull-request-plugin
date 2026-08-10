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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.plugins.git.UserRemoteConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerNotificationConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRApiResponse;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRNotificationSender;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * The credential is resolved against a different URL depending on the flavor: on Server against the
 * configured destination, on Cloud against the configured SCM URL, since
 * the Cloud notification endpoint is the fixed API host while a domain-scoped credential is bound to
 * the repository host. This pins that distinction so it is not accidentally collapsed later.
 */
class DefaultBitBucketPPRClientTest {

  private static final String SCM_URL = "https://bitbucket.org/some-namespace/some-repo.git";
  private static final String CLOUD_DESTINATION =
      "https://api.bitbucket.org/2.0/repositories/x/y/commit/z/statuses/build";
  private static final String SERVER_DESTINATION =
      "https://bitbucket.example.org/rest/build-status/1.0/commits/deadbeef";
  private static final String SERVER_BASE = "https://bitbucket.example.org";
  private static final String SERVER_CRED = "server-cred";

  @Test
  void cloudResolvesTheCredentialAgainstTheScmHostNotTheApiHost() throws Exception {
    try (MockedStatic<BitBucketPPRNotificationSender> sender =
        mockStatic(BitBucketPPRNotificationSender.class)) {
      BitBucketPPREventContext context = mock(BitBucketPPREventContext.class);
      UserRemoteConfig scm = mock(UserRemoteConfig.class);
      when(scm.getUrl()).thenReturn(SCM_URL);
      when(context.getUserRemoteConfig()).thenReturn(scm);
      when(context.getStandardCredentials(anyString()))
          .thenReturn(mock(StandardUsernamePasswordCredentials.class));
      sender.when(() -> BitBucketPPRNotificationSender.sendBasicAuth(any(), any(), anyString(), any()))
          .thenReturn(new BitBucketPPRApiResponse(200, ""));

      new DefaultBitBucketPPRClient(BitBucketPPRClientType.CLOUD, context)
          .send(CLOUD_DESTINATION, "{}");

      verify(context).getStandardCredentials(SCM_URL);
      verify(context, never()).getStandardCredentials(CLOUD_DESTINATION);
    }
  }

  @Test
  void serverResolvesTheCredentialFromTheNotificationConfigPair() throws Exception {
    try (MockedStatic<BitBucketPPRNotificationSender> sender =
        mockStatic(BitBucketPPRNotificationSender.class)) {
      BitBucketPPREventContext context = mock(BitBucketPPREventContext.class);
      when(context.getNotificationConfig())
          .thenReturn(new BitBucketPPRServerNotificationConfig(SERVER_BASE, SERVER_CRED));
      when(context.getStandardCredentials(anyString(), anyString()))
          .thenReturn(mock(StandardUsernamePasswordCredentials.class));
      sender.when(() -> BitBucketPPRNotificationSender.sendBasicAuth(any(), any(), anyString(), any()))
          .thenReturn(new BitBucketPPRApiResponse(200, ""));

      new DefaultBitBucketPPRClient(BitBucketPPRClientType.SERVER, context)
          .send(SERVER_DESTINATION, "{}");

      // Endpoint and credential come from the same resolved pair, never from the SCM URL.
      verify(context).getStandardCredentials(SERVER_CRED, SERVER_BASE);
      verify(context, never()).getUserRemoteConfig();
    }
  }
}
