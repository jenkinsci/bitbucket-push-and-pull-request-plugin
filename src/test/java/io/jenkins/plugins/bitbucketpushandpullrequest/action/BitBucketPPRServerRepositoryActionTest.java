package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitBucketPPRServerRepositoryActionTest {

  @Test
  void testBaseUrlSet() {
    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      when(c.getPropagationUrl()).thenReturn(
          "https://example.org/scm/some-namespace/some-repo.git");

      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      List<BitBucketPPRServerClone> clones = new ArrayList<>();
      BitBucketPPRServerClone mockServerClone = mock(BitBucketPPRServerClone.class);
      when(mockServerClone.getName()).thenReturn("ssh");
      when(mockServerClone.getHref()).thenReturn(
          "ssh://git@example.org/some-namespace/some-repo.git");
      clones.add(mockServerClone);
      when(payloadMock.getServerRepository().getLinks().getCloneProperty()).thenReturn(
          clones);
      BitBucketPPRServerRepositoryAction bitBucketPPRServerRepositoryAction = new BitBucketPPRServerRepositoryAction(
          payloadMock);

      assertDoesNotThrow(bitBucketPPRServerRepositoryAction::getCommitLinks);
    }
  }
}
