package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRPayloadPropertyNotFoundException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class BitBucketPPRPullRequestServerActionTest {
  @Test
  public void testBaseUrlSet() {
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
      when(payloadMock.getServerRepository().getLinks().getCloneProperty()).thenReturn(clones);
      BitBucketPPRPullRequestServerAction bitBucketPPRPullRequestServerAction;
      try {
        BitBucketPPRHookEvent bitbucketEvent = mock(BitBucketPPRHookEvent.class);
        when(bitbucketEvent.getAction()).thenReturn("created");
        bitBucketPPRPullRequestServerAction =
            new BitBucketPPRPullRequestServerAction(payloadMock, bitbucketEvent);
        assertDoesNotThrow(
            () -> {
              bitBucketPPRPullRequestServerAction.getCommitLink();
            });
      } catch (BitBucketPPRPayloadPropertyNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void testGetMergeCommit() throws BitBucketPPRPayloadPropertyNotFoundException {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      when(payloadMock.getPullRequest().getMergeCommit().getHash()).thenReturn("123456");
      BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
      when(event.getAction()).thenReturn("fulfilled");
      BitBucketPPRPullRequestServerAction action =
          new BitBucketPPRPullRequestServerAction(payloadMock, event);
      assertEquals("123456", action.getLatestCommit());
    }
  }
}
