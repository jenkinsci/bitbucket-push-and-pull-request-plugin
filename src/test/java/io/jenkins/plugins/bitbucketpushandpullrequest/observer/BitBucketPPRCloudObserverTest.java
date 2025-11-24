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

package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitBucketPPRCloudObserverTest {

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
  void testPushCloudObserver() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRAction action = new BitBucketPPRRepositoryAction(payload);
      List<String> links = new ArrayList<>();
      links.add(
          "https://api.bitbucket.org/2.0/repositories/some-repository/some-repo/commit/09c4367c5bdbef7d7a28ba4cc2638488c2088d6b");

      assertEquals(links, action.getCommitLinks());

      BitBucketPPRPushCloudObserver spyObserver = Mockito.spy(
          BitBucketPPRPushCloudObserver.class);
      BitBucketPPREvent event = Mockito.mock(BitBucketPPREvent.class);
      BitBucketPPREventContext context = Mockito.mock(BitBucketPPREventContext.class);

      Mockito.when(context.getAbsoluteUrl()).thenReturn("https://someURL");
      Mockito.when(context.getBuildNumber()).thenReturn(12);
      Mockito.when(context.getAction()).thenReturn(action);
      Mockito.when(event.getContext()).thenReturn(context);
      Mockito.doReturn(c).when(spyObserver).getGlobalConfig();

      String url =
          "https://api.bitbucket.org/2.0/repositories/some-repository/some-repo/commit/09c4367c5bdbef7d7a28ba4cc2638488c2088d6b/statuses/build";
      Map<String, String> map = new HashMap<>();
      map.put("key", spyObserver.computeBitBucketBuildKey(context));
      map.put("state", "INPROGRESS");
      map.put("url", context.getAbsoluteUrl());

      spyObserver.getNotification(event);
      spyObserver.setBuildStatusInProgress();
      Mockito.verify(spyObserver).setBuildStatusInProgress();

      Mockito.verify(spyObserver).callClient(url, map);
    }
  }

  @Test
  void testComputeBitBucketBuildKeyForInProgressBuild() {
    BitBucketPPRPushCloudObserver spyObserver = Mockito.spy(
        BitBucketPPRPushCloudObserver.class);
    BitBucketPPREventContext context = Mockito.mock(BitBucketPPREventContext.class);
    BitBucketPPRPluginConfig config = Mockito.mock(BitBucketPPRPluginConfig.class);
    Run run = Mockito.mock(Run.class);
    Job job = Mockito.mock(Job.class);

    // Given that the job was just started with the below parameters
    int buildNumber = 12;
    String jobName = "unit test job";
    Mockito.when(job.getDisplayName()).thenReturn(jobName);
    Mockito.when(run.getParent()).thenReturn(job);
    Mockito.when(context.getRun()).thenReturn(run);
    Mockito.when(context.getBuildNumber()).thenReturn(buildNumber);
    Mockito.doReturn(config).when(spyObserver).getGlobalConfig();

    // When it's configured to not use the job name
    Mockito.when(config.getUseJobNameAsBuildKey()).thenReturn(false);

    // Then the build number shall be the key
    assertEquals(String.valueOf(buildNumber), spyObserver.computeBitBucketBuildKey(context));

    // When it's configured to use the job name
    Mockito.when(config.getUseJobNameAsBuildKey()).thenReturn(true);

    // Then the the job name shall be the key
    assertEquals(jobName, spyObserver.computeBitBucketBuildKey(context));
  }

  @Test
  void testComputeBitBucketBuildKeyForFinishedBuild() {
    BitBucketPPRPushCloudObserver spyObserver = Mockito.spy(
        BitBucketPPRPushCloudObserver.class);
    BitBucketPPREventContext context = Mockito.mock(BitBucketPPREventContext.class);
    BitBucketPPRPluginConfig config = Mockito.mock(BitBucketPPRPluginConfig.class);
    Run run = Mockito.mock(Run.class);
    Job job = Mockito.mock(Job.class);

    // Given that the job finished with the below parameters
    int buildNumber = 12;
    String jobName = "unit test job";
    Mockito.when(job.getDisplayName()).thenReturn(jobName);
    Mockito.when(run.getParent()).thenReturn(job);
    Mockito.when(context.getRun()).thenReturn(run);
    Mockito.when(context.getBuildNumber()).thenReturn(buildNumber);
    Mockito.doReturn(config).when(spyObserver).getGlobalConfig();

    // When it's configured to not use the job name
    Mockito.when(config.getUseJobNameAsBuildKey()).thenReturn(false);

    // Then the build number shall be the key
    assertEquals(String.valueOf(buildNumber), spyObserver.computeBitBucketBuildKey(context));

    // When it's configured to use the job name
    Mockito.when(config.getUseJobNameAsBuildKey()).thenReturn(true);

    // Then the the job name shall be the key
    assertEquals(jobName, spyObserver.computeBitBucketBuildKey(context));
  }
}
