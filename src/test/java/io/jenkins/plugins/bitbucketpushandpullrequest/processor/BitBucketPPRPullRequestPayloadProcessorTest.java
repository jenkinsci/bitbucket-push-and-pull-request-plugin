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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.naming.OperationNotSupportedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObserverFactory;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRPullRequestPayloadProcessorTest {

  @Captor
  private ArgumentCaptor<BitBucketPPRHookEvent> eventCaptor;

  @Captor
  private ArgumentCaptor<BitBucketPPRAction> actionCaptor;

  @Captor
  private ArgumentCaptor<BitBucketPPRObservable> observableCaptor;

  BitBucketPPRPullRequestCloudPayloadProcessor pullRequestPayloadProcessor;


  @Test
  public void testProcessPullRequestApprovalWebhookGit() throws OperationNotSupportedException {
    JsonReader reader = null;

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("./cloud/pr_approved.json");
      assert is != null;
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try (MockedStatic<BitBucketPPRPluginConfig> config = Mockito.mockStatic(
        BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRJobProbe probe = mock(BitBucketPPRJobProbe.class);

      Gson gson = new Gson();
      assert reader != null;
      BitBucketPPRPayload payload = gson.fromJson(reader, BitBucketPPRCloudPayload.class);

      BitBucketPPRHookEvent bitbucketEvent = new BitBucketPPRHookEvent("pullrequest:approved");
      pullRequestPayloadProcessor =
          new BitBucketPPRPullRequestCloudPayloadProcessor(probe, bitbucketEvent);

      BitBucketPPRObservable observable =
          BitBucketPPRObserverFactory.createObservable(bitbucketEvent);
      pullRequestPayloadProcessor.processPayload(payload, observable);

      verify(probe).triggerMatchingJobs(eventCaptor.capture(), actionCaptor.capture(),
          observableCaptor.capture());

      assertEquals(bitbucketEvent, eventCaptor.getValue());
      assertEquals(payload, actionCaptor.getValue().getPayload());
      assertEquals(observable, observableCaptor.getValue());
    }
  }
}
