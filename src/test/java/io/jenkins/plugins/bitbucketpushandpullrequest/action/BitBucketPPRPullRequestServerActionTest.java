package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class BitBucketPPRPullRequestServerActionTest {

  @Test
  void testBaseUrlSet() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      when(c.isPropagationUrlSet()).thenReturn(true);
      when(c.getPropagationUrl())
          .thenReturn("https://example.org/scm/some-namespace/some-repo.git");

      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      List<BitBucketPPRServerClone> clones = new ArrayList<>();
      BitBucketPPRServerClone mockServerClone = mock(BitBucketPPRServerClone.class);
      when(mockServerClone.getName()).thenReturn("ssh");
      when(mockServerClone.getHref())
          .thenReturn("ssh://git@example.org/some-namespace/some-repo.git");
      clones.add(mockServerClone);
      when(payloadMock.getServerRepository().getLinks().getCloneProperty()).thenReturn(
          clones);

      BitBucketPPRHookEvent bitbucketEvent = mock(BitBucketPPRHookEvent.class);
      when(bitbucketEvent.getAction()).thenReturn("created");
      BitBucketPPRPullRequestServerAction bitBucketPPRPullRequestServerAction =
          new BitBucketPPRPullRequestServerAction(payloadMock, bitbucketEvent);

      assertDoesNotThrow(bitBucketPPRPullRequestServerAction::getCommitLink);
    }
  }

  @Test
  void testGetMergeCommit() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      when(payloadMock.getServerPullRequest().getProperties().getMergeCommit()
          .getId()).thenReturn("123456");
      BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
      when(event.getAction()).thenReturn("merged");
      BitBucketPPRPullRequestServerAction action =
          new BitBucketPPRPullRequestServerAction(payloadMock, event);
      assertEquals("123456", action.getLatestCommit());
    }
  }
}
