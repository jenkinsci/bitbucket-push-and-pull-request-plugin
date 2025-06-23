package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserverFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitBucketPPRRepositoryPayloadProcessorTest {

  @Captor
  private ArgumentCaptor<BitBucketPPRHookEvent> eventCaptor;
  @Captor
  private ArgumentCaptor<BitBucketPPRAction> actionCaptor;
  @Captor
  private ArgumentCaptor<BitBucketPPRObservable> observableCaptor;

  private BitBucketPPRPayload payload;
  private BitBucketPPRHookEvent bitbucketEvent;

  @BeforeEach
  void setUp() throws Exception {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("./cloud/repo_push.json");
    assertNotNull(is);
    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
    JsonReader reader = new JsonReader(isr);

    Gson gson = new Gson();
    this.payload = gson.fromJson(reader, BitBucketPPRCloudPayload.class);
    this.bitbucketEvent = new BitBucketPPRHookEvent("repo:push");
  }

  @Test
  void testRepositoryPushWebhookGit() {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig configInstance = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(configInstance);
      BitBucketPPRJobProbe probe = mock(BitBucketPPRJobProbe.class);

      BitBucketPPRRepositoryCloudPayloadProcessor repositoryPayloadProcessor =
          new BitBucketPPRRepositoryCloudPayloadProcessor(probe, this.bitbucketEvent);

      BitBucketPPRObservable observable = BitBucketPPRObserverFactory.createObservable(
          bitbucketEvent);

      repositoryPayloadProcessor.processPayload(payload, observable);

      verify(probe)
          .triggerMatchingJobs(
              eventCaptor.capture(), actionCaptor.capture(), observableCaptor.capture());

      assertEquals(bitbucketEvent, eventCaptor.getValue());
      assertEquals(payload, actionCaptor.getValue().getPayload());
      assertEquals(observable, observableCaptor.getValue());
    }
  }
}
