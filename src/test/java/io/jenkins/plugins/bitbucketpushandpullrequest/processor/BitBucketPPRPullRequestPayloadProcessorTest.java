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

import javax.naming.OperationNotSupportedException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;
import net.sf.json.JSONObject;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRPullRequestPayloadProcessorTest {
  Gson gson = new Gson();
  @Mock
  private BitBucketPPRJobProbe probe;

  @Captor
  private ArgumentCaptor<BitBucketPPREvent> eventCaptor;
  @Captor
  private ArgumentCaptor<BitBucketPPRAction> actionCaptor;

  private BitBucketPPRPullRequestPayloadProcessor pullRequestPayloadProcessor;

  @Test
  public void testProcessPullRequestApprovalWebhookHg() {
    String user = "test_user";
    String url = "https://bitbucket.org/test_user/test_repo";

    BitBucketPPREvent bitbucketEvent = null;
    try {
      bitbucketEvent = new BitBucketPPREvent("pullrequest:approved");
    } catch (OperationNotSupportedException e) {
      e.printStackTrace();
    }

    pullRequestPayloadProcessor =
        new BitBucketPPRPullRequestPayloadProcessor(probe, bitbucketEvent);

    JSONObject inputStream = new JSONObject().element("scm", "hg")
        .element("owner", new JSONObject().element("username", user))
        .element("links", new JSONObject().element("html", new JSONObject().element("href", url)));

    BitBucketPPRPayload payload =
        gson.fromJson(inputStream.toString(), BitBucketPPRNewPayload.class);


    pullRequestPayloadProcessor.processPayload(payload);

    verify(probe).triggerMatchingJobs(eventCaptor.capture(), actionCaptor.capture());

    assertEquals(bitbucketEvent, eventCaptor.getValue());
    assertEquals(payload, actionCaptor.getValue().getPayload());
  }


  @Test
  public void testProcessPullRequestApprovalWebhookGit() {
    BitBucketPPREvent bitbucketEvent = null;
    try {
      bitbucketEvent = new BitBucketPPREvent("pullrequest:approved");
    } catch (OperationNotSupportedException e) {
      e.printStackTrace();
    }

    pullRequestPayloadProcessor =
        new BitBucketPPRPullRequestPayloadProcessor(probe, bitbucketEvent);

    JsonReader reader = null;

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("pullrequest_approved.json");
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    BitBucketPPRPayload payload = null;
    try {
      payload = gson.fromJson(reader, BitBucketPPRNewPayload.class);
    } catch (JsonIOException e) {
      e.printStackTrace();
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    }
    
    try {
      pullRequestPayloadProcessor.processPayload(payload);
    } catch (Exception e) {
      e.printStackTrace();
    }

    verify(probe).triggerMatchingJobs(eventCaptor.capture(), actionCaptor.capture());

    assertEquals(bitbucketEvent, eventCaptor.getValue());
    assertEquals(payload, actionCaptor.getValue().getPayload());
  }
}
