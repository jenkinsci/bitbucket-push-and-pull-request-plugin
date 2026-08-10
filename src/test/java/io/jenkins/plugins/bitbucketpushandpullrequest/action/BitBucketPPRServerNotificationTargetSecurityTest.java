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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerChange;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Pins the trust boundary of the Bitbucket Server notification path.
 *
 * <p>The destination of an authenticated build-status notification is derived only from the
 * Bitbucket server configured for the job (the propagation URL, resolved trigger-then-global), never
 * from the clone URLs carried in the webhook payload. A clone URL that is present only in the payload
 * may at most take part in the SCM match that decides whether a job builds; it must never become the
 * endpoint to which an authenticated request (carrying the job credential) is sent. When no server is
 * configured the notification is refused rather than sent to a payload-derived host.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitBucketPPRServerNotificationTargetSecurityTest {

  private static final String CONFIGURED_SERVER = "https://bitbucket.example.org";
  private static final String UNCONFIGURED_HOST = "other.example";
  private static final String PAYLOAD_CLONE_ON_UNCONFIGURED_HOST =
      "http://other.example/scm/PROJ/repo.git";

  @Test
  void prNotificationTargetComesFromTheConfiguredServerNotThePayload() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      stubGlobalConfig(config);

      BitBucketPPRPullRequestServerAction action =
          pullRequestActionWithToRefClone(PAYLOAD_CLONE_ON_UNCONFIGURED_HOST);
      action.setPropagationUrl(CONFIGURED_SERVER);

      String target = action.getCommitLink();

      assertTrue(target.startsWith(CONFIGURED_SERVER),
          "notification must target the configured server, was: " + target);
      assertFalse(target.contains(UNCONFIGURED_HOST),
          "payload clone host must never appear in the notification target, was: " + target);
    }
  }

  @Test
  void prRefusesToNotifyWhenNoServerIsConfigured() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      stubGlobalConfig(config);

      // No propagation URL set on the trigger and none globally: there is no configured
      // destination, so the notification is refused instead of falling back to the payload clone.
      BitBucketPPRPullRequestServerAction action =
          pullRequestActionWithToRefClone(PAYLOAD_CLONE_ON_UNCONFIGURED_HOST);

      assertThrows(RuntimeException.class, action::getCommitLink);
    }
  }

  @Test
  void prPayloadCloneReachesTheScmMatchButNotTheNotificationTarget() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      stubGlobalConfig(config);

      BitBucketPPRPullRequestServerAction action =
          pullRequestActionWithToRefClone(PAYLOAD_CLONE_ON_UNCONFIGURED_HOST);
      action.setPropagationUrl(CONFIGURED_SERVER);

      // The payload clone still contributes to the SCM match set (this is how the job is selected),
      // but that set is not the egress policy: it does not reach the notification target.
      assertTrue(action.getScmUrls().contains(PAYLOAD_CLONE_ON_UNCONFIGURED_HOST),
          "payload clone should be part of the SCM match set");
      assertFalse(action.getCommitLink().contains(UNCONFIGURED_HOST),
          "match set membership must not turn the payload host into the notification target");
    }
  }

  @Test
  void pushNotificationTargetComesFromTheConfiguredServerNotThePayload() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      stubGlobalConfig(config);

      BitBucketPPRServerRepositoryAction action =
          pushActionWithRepositoryClone(PAYLOAD_CLONE_ON_UNCONFIGURED_HOST);
      action.setPropagationUrl(CONFIGURED_SERVER);

      List<String> targets = action.getCommitLinks();

      assertFalse(targets.isEmpty(), "the push carries a change, so a target is produced");
      for (String target : targets) {
        assertTrue(target.startsWith(CONFIGURED_SERVER),
            "notification must target the configured server, was: " + target);
        assertFalse(target.contains(UNCONFIGURED_HOST),
            "payload clone host must never appear in the notification target, was: " + target);
      }
    }
  }

  private static void stubGlobalConfig(MockedStatic<BitBucketPPRPluginConfig> config) {
    BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
    config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
  }

  private static BitBucketPPRPullRequestServerAction pullRequestActionWithToRefClone(String href)
      throws Exception {
    BitBucketPPRPayload payload = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);

    List<BitBucketPPRServerClone> toClones = new ArrayList<>();
    BitBucketPPRServerClone toClone = mock(BitBucketPPRServerClone.class);
    when(toClone.getName()).thenReturn("http");
    when(toClone.getHref()).thenReturn(href);
    toClones.add(toClone);
    when(payload.getServerPullRequest().getToRef().getRepository().getLinks().getCloneProperty())
        .thenReturn(toClones);
    when(payload.getServerPullRequest().getFromRef().getRepository().getLinks().getCloneProperty())
        .thenReturn(new ArrayList<>());
    when(payload.getServerPullRequest().getFromRef().getLatestCommit()).thenReturn("deadbeef");

    BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
    when(event.getAction()).thenReturn("created");
    return new BitBucketPPRPullRequestServerAction(payload, event);
  }

  private static BitBucketPPRServerRepositoryAction pushActionWithRepositoryClone(String href)
      throws Exception {
    BitBucketPPRPayload payload = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);

    List<BitBucketPPRServerClone> clones = new ArrayList<>();
    BitBucketPPRServerClone clone = mock(BitBucketPPRServerClone.class);
    when(clone.getName()).thenReturn("http");
    when(clone.getHref()).thenReturn(href);
    clones.add(clone);
    when(payload.getServerRepository().getLinks().getCloneProperty()).thenReturn(clones);

    List<BitBucketPPRServerChange> changes = new ArrayList<>();
    BitBucketPPRServerChange change = mock(BitBucketPPRServerChange.class, RETURNS_DEEP_STUBS);
    when(change.getRefId()).thenReturn("refs/heads/main");
    when(change.getToHash()).thenReturn("deadbeef");
    changes.add(change);
    when(payload.getServerChanges()).thenReturn(changes);

    return new BitBucketPPRServerRepositoryAction(payload);
  }
}
