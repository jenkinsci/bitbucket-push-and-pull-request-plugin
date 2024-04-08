package io.jenkins.plugins.bitbucketpushandpullrequest.observer;


import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.OperationNotSupportedException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import hudson.model.Job;
import hudson.model.Run;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRCloudObserverTest {
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
  public void testPushCloudObserver() throws Throwable {
    BitBucketPPRAction action = new BitBucketPPRRepositoryAction(payload);
    List<String> links = new ArrayList<>();
    links.add(
        "https://api.bitbucket.org/2.0/repositories/some-repository/some-repo/commit/09c4367c5bdbef7d7a28ba4cc2638488c2088d6b");

    assertEquals(links, action.getCommitLinks());

    BitBucketPPRPushCloudObserver spyObserver = Mockito.spy(BitBucketPPRPushCloudObserver.class);
    BitBucketPPREvent event = Mockito.mock(BitBucketPPREvent.class);
    BitBucketPPREventContext context = Mockito.mock(BitBucketPPREventContext.class);
    BitBucketPPRPluginConfig config = Mockito.mock(BitBucketPPRPluginConfig.class);

    Mockito.when(context.getAbsoluteUrl()).thenReturn("https://someURL");
    Mockito.when(context.getBuildNumber()).thenReturn(12);
    Mockito.when(context.getAction()).thenReturn(action);
    Mockito.when(event.getContext()).thenReturn(context);
    Mockito.doReturn(config).when(spyObserver).getGlobalConfig();

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

  @Test
  public void testComputeBitBucketBuildKeyForInProgressBuild() {
    BitBucketPPRPushCloudObserver spyObserver = Mockito.spy(BitBucketPPRPushCloudObserver.class);
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
  public void testComputeBitBucketBuildKeyForFinishedBuild() {
    BitBucketPPRPushCloudObserver spyObserver = Mockito.spy(BitBucketPPRPushCloudObserver.class);
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
