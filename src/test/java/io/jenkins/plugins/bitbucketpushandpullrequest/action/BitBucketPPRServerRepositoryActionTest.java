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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRPayloadPropertyNotFoundException;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitBucketPPRServerRepositoryActionTest {

  @Test
  void constructorThrowsWhenServerRepositoryMissing() {
    // A malformed push payload with a null server 'repository' must be rejected with a clear
    // checked exception instead of a cryptic NullPointerException downstream (issue #384).
    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class);
      assertThrows(BitBucketPPRPayloadPropertyNotFoundException.class,
          () -> new BitBucketPPRServerRepositoryAction(payloadMock));
    }
  }

  @Test
  void testBaseUrlSet() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

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
      // The resolved notification base is set on the action at the sink; simulate that here.
      bitBucketPPRServerRepositoryAction.setPropagationUrl("https://example.org");

      assertDoesNotThrow(bitBucketPPRServerRepositoryAction::getCommitLinks);
    }
  }

  @Test
  void testGetCommitLinksWithNullBaseUrlAndNoPropagationUrl() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      List<BitBucketPPRServerClone> clones = new ArrayList<>();
      BitBucketPPRServerClone sshClone = mock(BitBucketPPRServerClone.class);
      when(sshClone.getName()).thenReturn("ssh");
      when(sshClone.getHref()).thenReturn("ssh://git@example.org/ns/repo.git");
      clones.add(sshClone);
      when(payloadMock.getServerRepository().getLinks().getCloneProperty()).thenReturn(clones);

      BitBucketPPRServerRepositoryAction action = new BitBucketPPRServerRepositoryAction(payloadMock);

      assertThrows(RuntimeException.class,
        action::getCommitLinks);
    }
  }

  @Test
  void testGetOPT1CloneUrlWithEmptyClones() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      // Constructor needs at least one clone
      List<BitBucketPPRServerClone> constructorClones = new ArrayList<>();
      BitBucketPPRServerClone sshClone = mock(BitBucketPPRServerClone.class);
      when(sshClone.getName()).thenReturn("ssh");
      when(sshClone.getHref()).thenReturn("ssh://git@example.org/ns/repo.git");
      constructorClones.add(sshClone);
      when(payloadMock.getServerRepository().getLinks().getCloneProperty())
          .thenReturn(constructorClones)
          .thenReturn(new ArrayList<>());

      BitBucketPPRServerRepositoryAction action = new BitBucketPPRServerRepositoryAction(payloadMock);

      assertNull(action.getOPT1CloneUrl());
    }
  }

  @Test
  void testGetOPT2CloneUrlWithSingleClone() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRPayload payloadMock = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      List<BitBucketPPRServerClone> clones = new ArrayList<>();
      BitBucketPPRServerClone sshClone = mock(BitBucketPPRServerClone.class);
      when(sshClone.getName()).thenReturn("ssh");
      when(sshClone.getHref()).thenReturn("ssh://git@example.org/ns/repo.git");
      clones.add(sshClone);
      when(payloadMock.getServerRepository().getLinks().getCloneProperty()).thenReturn(clones);

      BitBucketPPRServerRepositoryAction action = new BitBucketPPRServerRepositoryAction(payloadMock);

      assertNull(action.getOPT2CloneUrl());
    }
  }

  @Test
  void getCommitLinksPreserveTheConfiguredContextPath() throws Exception {
    // The REST path is appended to the configured base including its context path (reverse proxy),
    // not to the bare origin.
    assertEquals(
        List.of("https://bitbucket.example.com/bitbucket/rest/build-status/1.0/commits/123456"),
        commitLinksFor("https://bitbucket.example.com/bitbucket"));
  }

  @Test
  void getCommitLinksNormaliseATrailingSlashOnTheConfiguredBase() throws Exception {
    assertEquals(
        List.of("https://bitbucket.example.com/bitbucket/rest/build-status/1.0/commits/123456"),
        commitLinksFor("https://bitbucket.example.com/bitbucket/"));
  }

  private static List<String> commitLinksFor(String propagationUrl) throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRPayload payload = mock(BitBucketPPRPayload.class, RETURNS_DEEP_STUBS);
      List<BitBucketPPRServerClone> clones = new ArrayList<>();
      BitBucketPPRServerClone sshClone = mock(BitBucketPPRServerClone.class);
      when(sshClone.getName()).thenReturn("ssh");
      when(sshClone.getHref()).thenReturn("ssh://git@example.org/ns/repo.git");
      clones.add(sshClone);
      when(payload.getServerRepository().getLinks().getCloneProperty()).thenReturn(clones);

      List<BitBucketPPRServerChange> changes = new ArrayList<>();
      BitBucketPPRServerChange change = mock(BitBucketPPRServerChange.class, RETURNS_DEEP_STUBS);
      when(change.getRefId()).thenReturn("refs/heads/main");
      when(change.getToHash()).thenReturn("123456");
      changes.add(change);
      when(payload.getServerChanges()).thenReturn(changes);

      BitBucketPPRServerRepositoryAction action = new BitBucketPPRServerRepositoryAction(payload);
      action.setPropagationUrl(propagationUrl);
      return action.getCommitLinks();
    }
  }
}
