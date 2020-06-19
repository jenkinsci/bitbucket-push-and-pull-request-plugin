package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.naming.OperationNotSupportedException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserverFactory;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRRepositoryPayloadProcessorTest {

  @Mock
  private BitBucketPPRJobProbe probe;

  @Captor
  private ArgumentCaptor<BitBucketPPRHookEvent> eventCaptor;

  @Captor
  private ArgumentCaptor<BitBucketPPRAction> actionCaptor;

  @Captor
  private ArgumentCaptor<BitBucketPPRObservable> observableCaptor;


  public BitBucketPPRPayload payload;
  public BitBucketPPRHookEvent bitbucketEvent;

  @Before
  public void readPayload() {
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
    this.payload = gson.fromJson(reader, BitBucketPPRCloudPayload.class);

    try {
      this.bitbucketEvent = new BitBucketPPRHookEvent("repo:push");
    } catch (OperationNotSupportedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testRepositoryPushWebhookGit() {
    BitBucketPPRRepositoryCloudPayloadProcessor repositoryPayloadProcessor =
        new BitBucketPPRRepositoryCloudPayloadProcessor(probe, this.bitbucketEvent);


    BitBucketPPRObservable observable = null;
    try {
      observable = BitBucketPPRObserverFactory.createObservable(bitbucketEvent);
    } catch (Exception e) {
      e.printStackTrace();
    }

    repositoryPayloadProcessor.processPayload(payload, observable);

    verify(probe).triggerMatchingJobs(eventCaptor.capture(), actionCaptor.capture(),
        observableCaptor.capture());

    assertEquals(bitbucketEvent, eventCaptor.getValue());
    assertEquals(payload, actionCaptor.getValue().getPayload());
    assertEquals(observable, observableCaptor.getValue());
  }
}
