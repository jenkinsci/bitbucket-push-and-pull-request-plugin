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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestServerAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerNotificationConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRApiResponse;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.api.BitBucketPPRNotificationSender;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * End-to-end check of the notification path: a payload whose toRef clone points at a host that is
 * not the configured server takes part in the SCM match set, but the authenticated request that
 * carries the job credential is sent only to the configured server, resolved together with its
 * credential.
 */
class BitBucketPPRServerNotificationEgressTest {

  private static final String CONFIGURED_BASE = "https://bitbucket.example.org";
  private static final String CONFIGURED_CRED = "configured-cred";
  private static final String CONFIGURED_SCM = "https://bitbucket.example.org/scm/PROJ/repo.git";
  private static final String UNCONFIGURED_CLONE = "http://other.example/scm/PROJ/repo.git";

  @Test
  void anUnconfiguredToRefNeverReachesTheSenderAndTheConfiguredPairIsUsed() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> cfg = mockStatic(BitBucketPPRPluginConfig.class);
        MockedStatic<BitBucketPPRNotificationSender> sender =
            mockStatic(BitBucketPPRNotificationSender.class)) {
      cfg.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRPullRequestServerAction action = payloadAction(UNCONFIGURED_CLONE, CONFIGURED_SCM);
      // The observer's sink sets the resolved notification base on the action before building the
      // URL; the toRef clone carried by the payload never becomes the destination.
      action.setPropagationUrl(CONFIGURED_BASE);
      assertTrue(action.getScmUrls().contains(UNCONFIGURED_CLONE),
          "the payload clone still takes part in the SCM match set");

      BitBucketPPREventContext context = mock(BitBucketPPREventContext.class);
      when(context.getNotificationConfig())
          .thenReturn(new BitBucketPPRServerNotificationConfig(CONFIGURED_BASE, CONFIGURED_CRED));
      when(context.getStandardCredentials(CONFIGURED_CRED, CONFIGURED_BASE))
          .thenReturn(mock(StandardUsernamePasswordCredentials.class));

      List<String> sentUrls = new ArrayList<>();
      sender.when(() -> BitBucketPPRNotificationSender.sendBasicAuth(any(), any(), anyString(), any()))
          .thenAnswer(inv -> {
            sentUrls.add(inv.getArgument(2));
            return new BitBucketPPRApiResponse(200, "");
          });

      new DefaultBitBucketPPRClient(BitBucketPPRClientType.SERVER, context)
          .send(action.getCommitLink(), "{}");

      // The credential was looked up as the configured pair, never against the SCM URL.
      verify(context).getStandardCredentials(CONFIGURED_CRED, CONFIGURED_BASE);
      assertFalse(sentUrls.isEmpty(), "the notification was sent");
      for (String url : sentUrls) {
        assertTrue(url.startsWith(CONFIGURED_BASE), "sent to the configured server, was: " + url);
        assertFalse(url.contains("other.example"), "the unconfigured host must never be reached: " + url);
      }
    }
  }

  private static BitBucketPPRPullRequestServerAction payloadAction(String toRefClone, String fromRefClone)
      throws Exception {
    BitBucketPPRPayload payload = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);

    List<BitBucketPPRServerClone> toClones = new ArrayList<>();
    BitBucketPPRServerClone toClone = mock(BitBucketPPRServerClone.class);
    when(toClone.getName()).thenReturn("http");
    when(toClone.getHref()).thenReturn(toRefClone);
    toClones.add(toClone);
    when(payload.getServerPullRequest().getToRef().getRepository().getLinks().getCloneProperty())
        .thenReturn(toClones);

    List<BitBucketPPRServerClone> fromClones = new ArrayList<>();
    BitBucketPPRServerClone fromClone = mock(BitBucketPPRServerClone.class);
    when(fromClone.getName()).thenReturn("http");
    when(fromClone.getHref()).thenReturn(fromRefClone);
    fromClones.add(fromClone);
    when(payload.getServerPullRequest().getFromRef().getRepository().getLinks().getCloneProperty())
        .thenReturn(fromClones);
    when(payload.getServerPullRequest().getFromRef().getLatestCommit()).thenReturn("deadbeef");

    BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
    when(event.getAction()).thenReturn("created");
    return new BitBucketPPRPullRequestServerAction(payload, event);
  }
}
