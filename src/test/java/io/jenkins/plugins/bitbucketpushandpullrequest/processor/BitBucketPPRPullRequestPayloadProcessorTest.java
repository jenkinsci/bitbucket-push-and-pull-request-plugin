/*
 * The MIT License
 *
 * Copyright (c) 2018 CloudBees, Inc.
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
 */

package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserver;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserverFactory;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRPullRequestPayloadProcessorTest {
  @Mock
  private BitBucketPPRJobProbe probe;

  @Captor
  private ArgumentCaptor<BitBucketPPREvent> eventCaptor;

  @Captor
  private ArgumentCaptor<BitBucketPPRAction> actionCaptor;

  @Captor
  private ArgumentCaptor<List<BitBucketPPRObserver>> observersCaptor;

  BitBucketPPRPullRequestCloudPayloadProcessor pullRequestPayloadProcessor;


  @Test
  public void testProcessPullRequestApprovalWebhookGit() {
    JsonReader reader = null;

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("./cloud/pr_approved.json");
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Gson gson = new Gson();
    BitBucketPPRPayload payload = gson.fromJson(reader, BitBucketPPRCloudPayload.class);

    BitBucketPPREvent bitbucketEvent = null;
    try {
      bitbucketEvent = new BitBucketPPREvent("pullrequest:approved");
    } catch (OperationNotSupportedException e) {
      e.printStackTrace();
    }

    pullRequestPayloadProcessor =
        new BitBucketPPRPullRequestCloudPayloadProcessor(probe, bitbucketEvent);


    List<BitBucketPPRObserver> observers = new ArrayList<>();
    try {
      observers = BitBucketPPRObserverFactory.createObservers(bitbucketEvent);
    } catch (Exception e) {
      e.printStackTrace();
    }

    pullRequestPayloadProcessor.processPayload(payload, observers);

    verify(probe).triggerMatchingJobs(eventCaptor.capture(), actionCaptor.capture(),
        observersCaptor.capture());

    assertEquals(bitbucketEvent, eventCaptor.getValue());
    assertEquals(payload, actionCaptor.getValue().getPayload());
    assertEquals(observers, observersCaptor.getValue());
  }
}
