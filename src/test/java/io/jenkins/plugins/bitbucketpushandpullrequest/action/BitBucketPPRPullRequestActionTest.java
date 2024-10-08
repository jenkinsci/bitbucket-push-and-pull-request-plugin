package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BitBucketPPRPullRequestActionTest {

  @Test
  public void testGetMergeCommit() {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      when(payloadMock.getRepository().getLinks().getHtml().getHref())
          .thenReturn("https://bitbucket.org/testproject/test-repo");
      when(payloadMock.getPullRequest().getMergeCommit().getHash()).thenReturn("123456");
      BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
      when(event.getAction()).thenReturn("fulfilled");
      BitBucketPPRPullRequestAction action = new BitBucketPPRPullRequestAction(payloadMock, event);
      assertEquals("123456", action.getLatestCommit());
    }
  }
}
