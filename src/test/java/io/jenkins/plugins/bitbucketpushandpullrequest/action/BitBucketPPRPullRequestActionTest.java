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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRPayloadPropertyNotFoundException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class BitBucketPPRPullRequestActionTest {

  @Test
  void testGetMergeCommit() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      when(payloadMock.getRepository().getLinks().getHtml().getHref())
          .thenReturn("https://bitbucket.org/testproject/test-repo");
      when(payloadMock.getPullRequest().getId()).thenReturn("1");
      when(payloadMock.getPullRequest().getMergeCommit().getHash()).thenReturn("123456");
      BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
      when(event.getAction()).thenReturn("fulfilled");
      BitBucketPPRPullRequestAction action = new BitBucketPPRPullRequestAction(payloadMock,
          event);
      assertEquals("123456", action.getLatestCommit());
    }
  }

  @Test
  void constructorThrowsWhenPullRequestMissing() {
    // A malformed/truncated webhook body can deserialize to a payload whose 'pullrequest'
    // property is null. The constructor must reject it with a clear, checked exception instead
    // of throwing a cryptic NullPointerException downstream (issue #384).
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class);
      when(payloadMock.getPullRequest()).thenReturn(null);
      BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
      assertThrows(BitBucketPPRPayloadPropertyNotFoundException.class,
          () -> new BitBucketPPRPullRequestAction(payloadMock, event));
    }
  }

  @Test
  void constructorThrowsWhenRepositoryMissing() {
    // 'pullrequest' present but 'repository' missing must also be rejected: the constructor
    // dereferences payload.getRepository() and would otherwise NPE (issue #384 follow-up).
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      when(payloadMock.getRepository()).thenReturn(null);
      BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
      assertThrows(BitBucketPPRPayloadPropertyNotFoundException.class,
          () -> new BitBucketPPRPullRequestAction(payloadMock, event));
    }
  }
}
