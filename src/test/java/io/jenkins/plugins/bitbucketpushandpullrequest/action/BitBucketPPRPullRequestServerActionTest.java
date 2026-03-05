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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

  @Test
  void testScmUrlsIncludeFromRefForForkedPR() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);

      // toRef clones (target repo)
      List<BitBucketPPRServerClone> toClones = new ArrayList<>();
      BitBucketPPRServerClone toHttpClone = mock(BitBucketPPRServerClone.class);
      when(toHttpClone.getName()).thenReturn("https");
      when(toHttpClone.getHref()).thenReturn("https://bitbucket.example.org/scm/target/repo.git");
      toClones.add(toHttpClone);
      when(payloadMock.getServerPullRequest().getToRef().getRepository().getLinks()
          .getCloneProperty()).thenReturn(toClones);

      // fromRef clones (fork repo - different URL)
      List<BitBucketPPRServerClone> fromClones = new ArrayList<>();
      BitBucketPPRServerClone fromHttpClone = mock(BitBucketPPRServerClone.class);
      when(fromHttpClone.getName()).thenReturn("https");
      when(fromHttpClone.getHref()).thenReturn("https://bitbucket.example.org/scm/fork/repo.git");
      fromClones.add(fromHttpClone);
      BitBucketPPRServerClone fromSshClone = mock(BitBucketPPRServerClone.class);
      when(fromSshClone.getName()).thenReturn("ssh");
      when(fromSshClone.getHref()).thenReturn("ssh://git@bitbucket.example.org/fork/repo.git");
      fromClones.add(fromSshClone);
      when(payloadMock.getServerPullRequest().getFromRef().getRepository().getLinks()
          .getCloneProperty()).thenReturn(fromClones);

      BitBucketPPRHookEvent bitbucketEvent = mock(BitBucketPPRHookEvent.class);
      when(bitbucketEvent.getAction()).thenReturn("created");
      BitBucketPPRPullRequestServerAction action =
          new BitBucketPPRPullRequestServerAction(payloadMock, bitbucketEvent);

      List<String> scmUrls = action.getScmUrls();
      assertTrue(scmUrls.contains("https://bitbucket.example.org/scm/target/repo.git"));
      assertTrue(scmUrls.contains("https://bitbucket.example.org/scm/fork/repo.git"));
      assertTrue(scmUrls.contains("ssh://git@bitbucket.example.org/fork/repo.git"));
      assertEquals(3, scmUrls.size());
    }
  }
}
