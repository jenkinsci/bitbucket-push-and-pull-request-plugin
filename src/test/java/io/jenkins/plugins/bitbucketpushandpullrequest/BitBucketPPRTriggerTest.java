package io.jenkins.plugins.bitbucketpushandpullrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestServerAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
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
class BitBucketPPRTriggerTest {

  @Test
  void testTriggerUrlOverridesBaseUrl() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
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
      BitBucketPPRPullRequestServerAction bitBucketPPRServerRepositoryAction =
          new BitBucketPPRPullRequestServerAction(payloadMock, bitbucketEvent);
      BitBucketPPRTriggerFilter bitBucketPPRTriggerFilter = mock(
          BitBucketPPRTriggerFilter.class);
      BitBucketPPRTrigger bitBucketPPRTrigger =
          new BitBucketPPRTrigger(List.of(bitBucketPPRTriggerFilter));
      bitBucketPPRTrigger.setPropagationUrl(
          "https://example2.org/scm/some-namespace/some-repo2.git");
      BitBucketPPRHookEvent bitBucketHookEvent = mock(BitBucketPPRHookEvent.class);

      SCM scmTrigger = mock(SCM.class);
      BitBucketPPRObservable observable = mock(BitBucketPPRObservable.class);
      bitBucketPPRTrigger.onPost(
          bitBucketHookEvent, bitBucketPPRServerRepositoryAction, scmTrigger, observable);
      assertEquals(
          "https://example2.org:-1",
          bitBucketPPRServerRepositoryAction.getCommitLink()
              .split("/rest/build-status/1.0/commits/")[0]);
    }
  }
}
