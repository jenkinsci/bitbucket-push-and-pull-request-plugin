package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.naming.OperationNotSupportedException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRRepositoryPayloadProcessorTest {
  @Mock
  private BitBucketPPRJobProbe probe;

  @Captor
  private ArgumentCaptor<BitBucketPPREvent> eventCaptor;
  @Captor
  private ArgumentCaptor<BitBucketPPRAction> actionCaptor;

  @Test
  public void testRepositoryPushWebhookGit() {
    BitBucketPPREvent bitbucketEvent = null;
    try {
      bitbucketEvent = new BitBucketPPREvent("repo:push");
    } catch (OperationNotSupportedException e) {
      e.printStackTrace();
    }

    BitBucketPPRRepositoryPayloadProcessor repositoryPayloadProcessor =
        new BitBucketPPRRepositoryPayloadProcessor(probe, bitbucketEvent);

    JsonReader reader = null;

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("./cloud/repo_push.json");
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Gson gson = new Gson();
    BitBucketPPRPayload payload = gson.fromJson(reader, BitBucketPPRNewPayload.class);

    repositoryPayloadProcessor.processPayload(payload);

    verify(probe).triggerMatchingJobs(eventCaptor.capture(), actionCaptor.capture());

    assertEquals(bitbucketEvent, eventCaptor.getValue());
    assertEquals(payload, actionCaptor.getValue().getPayload());
  }
}
