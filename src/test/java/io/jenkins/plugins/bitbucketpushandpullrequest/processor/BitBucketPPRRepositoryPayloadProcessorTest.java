package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.naming.OperationNotSupportedException;

import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRNewPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRRepositoryPayloadProcessorTest {
  Gson gson = new Gson();
  @Mock
  private BitBucketPPRJobProbe probe;

  @Captor
  private ArgumentCaptor<BitBucketPPREvent> eventCaptor;
  @Captor
  private ArgumentCaptor<BitBucketPPRAction> actionCaptor;

  private BitBucketPPRRepositoryPayloadProcessor repositoryPayloadProcessor;

  @Test
  public void testRepositoryPushWebhookGit() {
    BitBucketPPREvent bitbucketEvent = null;
    try {
      bitbucketEvent = new BitBucketPPREvent("repo:push");
    } catch (OperationNotSupportedException e) {
      e.printStackTrace();
    }

    repositoryPayloadProcessor = new BitBucketPPRRepositoryPayloadProcessor(probe, bitbucketEvent);

    JsonReader reader = null;

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("repo_push.json");
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    BitBucketPPRPayload payload = gson.fromJson(reader, BitBucketPPRNewPayload.class);

    repositoryPayloadProcessor.processPayload(payload);

    verify(probe).triggerMatchingJobs(eventCaptor.capture(), actionCaptor.capture());

    assertEquals(bitbucketEvent, eventCaptor.getValue());
    assertEquals(payload, actionCaptor.getValue().getPayload());
  }
}
