/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2020, CloudBees, Inc.
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

package io.jenkins.plugins.bitbucketpushandpullrequest.environment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import hudson.EnvVars;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestServerAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.cloud.BitBucketPPRPullRequestCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.server.BitBucketPPRPullRequestServerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerPayload;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitBucketPPREnvironmentContributorTest {

  private EnvVars envVars;

  @BeforeEach
  void setUp() {
    envVars = spy(EnvVars.class);
  }

  @Test
  void buildEnvironmentForCloudRepoPushTest() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payload = getCloudPayload("./cloud/repo_push.json");

      BitBucketPPRRepositoryCause cause = mock(BitBucketPPRRepositoryCause.class);
      BitBucketPPRRepositoryAction bitBucketPPRRepositoryAction =
          new BitBucketPPRRepositoryAction(payload);
      when(cause.getRepositoryPayLoad()).thenReturn(bitBucketPPRRepositoryAction);
      when(cause.getHookEvent()).thenReturn("X-EVENT");

      // do
      runEnvironmentContributorForCause(cause);

      // assert
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH,
              "feature/do-not-merge"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.REPOSITORY_LINK,
              "https://bitbucket.org/some-repository/some-repo"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_REPOSITORY_URL,
              "https://bitbucket.org/some-repository/some-repo"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_REPOSITORY_UUID,
              "{6b5a1057-07ff-47c1-a65e-6c136cce4hj4}"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-van-me-nickname"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
      assertThat(
          envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
    }
  }

  @Test
  void buildEnvironmentForCloudPullRequestCreatedTest() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_created.json");

      BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
      BitBucketPPRPullRequestAction bitBucketPPRPullRequestAction =
          new BitBucketPPRPullRequestAction(payload, mock(BitBucketPPRHookEvent.class));
      when(cause.getPullRequestPayLoad()).thenReturn(bitBucketPPRPullRequestAction);
      when(cause.getHookEvent()).thenReturn("X-EVENT");

      // do
      runEnvironmentContributorForCause(cause);

      // assert
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH,
              "feature/do-not-merge"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
              "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
              "I have to push the pram a lot X."));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION,
              "Some description for PR"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
      assertThat(
          envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
    }
  }

  @Test
  void buildEnvironmentForCloudPullRequestMergedTest() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_fulfilled.json");

      BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
      BitBucketPPRPullRequestAction bitBucketPPRPullRequestAction =
          new BitBucketPPRPullRequestAction(payload, mock(BitBucketPPRHookEvent.class));
      when(cause.getPullRequestPayLoad()).thenReturn(bitBucketPPRPullRequestAction);
      when(cause.getHookEvent()).thenReturn("X-EVENT");

      // do
      runEnvironmentContributorForCause(cause);

      // assert
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH,
              "feature/do-not-merge"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
              "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
              "I have to push the pram a lot X."));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION,
              "Some description for PR"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
      assertThat(
          envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
    }
  }

  @Test
  void buildEnvironmentForCloudPullRequestDeclinedTest() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_rejected.json");

      BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
      BitBucketPPRPullRequestAction bitBucketPPRPullRequestAction =
          new BitBucketPPRPullRequestAction(payload, mock(BitBucketPPRHookEvent.class));
      when(cause.getPullRequestPayLoad()).thenReturn(bitBucketPPRPullRequestAction);
      when(cause.getHookEvent()).thenReturn("X-EVENT");

      // do
      runEnvironmentContributorForCause(cause);

      // assert
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH,
              "feature/do-not-merge"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
              "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
              "I have to push the pram a lot X."));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION,
              "Some description for PR"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
      assertThat(
          envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
    }
  }

  @Test
  void buildEnvironmentForCloudPullRequestUpdatedTest() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_updated.json");

      BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
      BitBucketPPRPullRequestAction bitBucketPPRPullRequestAction =
          new BitBucketPPRPullRequestAction(payload, mock(BitBucketPPRHookEvent.class));
      when(cause.getPullRequestPayLoad()).thenReturn(bitBucketPPRPullRequestAction);
      when(cause.getHookEvent()).thenReturn("X-EVENT");

      // do
      runEnvironmentContributorForCause(cause);

      // assert
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH,
              "feature/do-not-merge"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
              "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
              "I have to push the pram a lot X."));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION,
              "Some description for PR"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
      assertThat(
          envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
    }
  }

  @Test
  void buildEnvironmentForCloudPullRequestApprovedTest() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_approved.json");

      BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
      BitBucketPPRPullRequestAction bitBucketPPRPullRequestAction =
          new BitBucketPPRPullRequestAction(payload, mock(BitBucketPPRHookEvent.class));
      when(cause.getPullRequestPayLoad()).thenReturn(bitBucketPPRPullRequestAction);
      when(cause.getHookEvent()).thenReturn("X-EVENT");

      // do
      runEnvironmentContributorForCause(cause);

      // assert
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH,
              "feature/do-not-merge"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
              "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
              "I have to push the pram a lot X."));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION,
              "Some description for PR"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
      assertThat(
          envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
    }
  }

  @Test
  void buildEnvironmentForCloudPullRequestCommentCreatedTest() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);
      BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_comment_created.json");

      BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
      BitBucketPPRPullRequestAction bitBucketPPRPullRequestAction =
          new BitBucketPPRPullRequestAction(payload, mock(BitBucketPPRHookEvent.class));
      when(cause.getPullRequestPayLoad()).thenReturn(bitBucketPPRPullRequestAction);
      when(cause.getHookEvent()).thenReturn("X-EVENT");

      // do
      runEnvironmentContributorForCause(cause);

      // assert
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH,
              "feature/do-not-merge"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH,
              "destination-branch"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
              "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
              "I have to push the pram a lot X."));
      assertThat(
          envVars,
          hasEntry(
              BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION,
              "Some description for PR"));
      assertThat(
          envVars,
          hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
      assertThat(
          envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
    }
  }

  @Disabled
  @Test
  void buildEnvironmentForServerPullRequestOpenedTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_opened.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad())
        .thenReturn(
            new BitBucketPPRPullRequestServerAction(payload,
                mock(BitBucketPPRHookEvent.class)));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "61"));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "http://example.org/projects/ABC/repos/some-repo/pullrequests/61"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Disabled
  @Test
  void buildEnvironmentForServerPullRequestModifiedTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_modified.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad())
        .thenReturn(
            new BitBucketPPRPullRequestServerAction(payload,
                mock(BitBucketPPRHookEvent.class)));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "http://example.org/projects/ABC/repos/some-repo/pull-requests/13"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "13"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Disabled
  @Test
  void buildEnvironmentForServerSourcePullRequestUpdatedTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_from_ref_updated.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad())
        .thenReturn(
            new BitBucketPPRPullRequestServerAction(payload,
                mock(BitBucketPPRHookEvent.class)));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "http://example.org/projects/ABC/repos/some-repo/pull-requests/61"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "1"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Disabled
  @Test
  void buildEnvironmentForServerPullRequestApprovedTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_reviewer_approved.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad())
        .thenReturn(
            new BitBucketPPRPullRequestServerAction(payload,
                mock(BitBucketPPRHookEvent.class)));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "http://example.org/projects/ABC/repos/some-repo/pull-requests/12"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "12"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "some-reviewer"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Disabled
  @Test
  void buildEnvironmentForServerPullRequestMergedTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_merged.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad())
        .thenReturn(
            new BitBucketPPRPullRequestServerAction(payload,
                mock(BitBucketPPRHookEvent.class)));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "http://example.org/projects/ABC/repos/some-repo/pull-requests/61"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "61"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Disabled
  @Test
  void buildEnvironmentForServerPullRequestDeclinedTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_declined.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad())
        .thenReturn(
            new BitBucketPPRPullRequestServerAction(payload,
                mock(BitBucketPPRHookEvent.class)));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "bugfix/tst-2"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "http://bitbucket:7990/projects/PPRPLUG/repos/hellophp/pull-requests/7"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "7"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
            "dummy change"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Disabled
  @Test
  void buildEnvironmentForServerRepoPushTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/repo_refs_changed.json");

    BitBucketPPRServerRepositoryCause cause = mock(BitBucketPPRServerRepositoryCause.class);
    when(cause.getServerRepositoryPayLoad())
        .thenReturn(new BitBucketPPRServerRepositoryAction(payload));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.REPOSITORY_NAME, "some-repo"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_REPOSITORY_ID, "99"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Disabled
  @Test
  void buildEnvironmentForServerCommentCreatedTest() throws Exception {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_comment_created.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad())
        .thenReturn(
            new BitBucketPPRPullRequestServerAction(payload,
                mock(BitBucketPPRHookEvent.class)));
    when(cause.getHookEvent()).thenReturn("X-EVENT");

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "test-pr2"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "https://bitbucket.company.com/bitbucket/users/username/repos/test-repo/pull-requests/2"));
    assertThat(
        envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "2"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "username"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "Test pr2"));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
    assertThat(
        envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PAYLOAD, payload.toString()));
    assertThat(
        envVars,
        hasEntry(
            BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_COMMENT_TEXT,
            "Comment content"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_X_EVENT, "X-EVENT"));
  }

  @Test
  void getBitbucketEventKeyPrOpenedTest() throws Exception {
    String hookEventAction = "pr:opened";
    String prefix = "jenkinsUnitTests";
    String suffix = "environmentContributor_getBitbucketEventKeyTest";
    File pollingLog = File.createTempFile(prefix, suffix);

    BitBucketPPRAction bitbucketAction = mock(BitBucketPPRAction.class);
    BitBucketPPRHookEvent bitBucketHookEvent = new BitBucketPPRHookEvent(hookEventAction);
    BitBucketPPRPullRequestCreatedActionFilter actionFilter =
        new BitBucketPPRPullRequestCreatedActionFilter();
    BitBucketPPRTriggerCause cause =
        actionFilter.getCause(pollingLog, bitbucketAction, bitBucketHookEvent);

    assertEquals(
        hookEventAction,
        cause.getHookEvent(),
        "Bitbuckethook event and hockEvent property of cause object are the same.");
  }

  @Test
  void getBitbucketEventKeyrepoRefsChangedTest() throws Exception {
    String hookEventAction = "repo:refs_changed";
    String prefix = "jenkinsUnitTests";
    String suffix = "environmentContributor_getBitbucketEventKeyTest";
    File pollingLog = File.createTempFile(prefix, suffix);

    BitBucketPPRAction bitbucketAction = mock(BitBucketPPRAction.class);
    BitBucketPPRHookEvent bitBucketHookEvent = new BitBucketPPRHookEvent(hookEventAction);

    // method params: boolean triggerAlsoIfTagPush, boolean
    // triggerAlsoIfNothingChanged, String allowedBranches
    BitBucketPPRRepositoryPushActionFilter actionFilter =
        new BitBucketPPRRepositoryPushActionFilter(false, false, null);
    BitBucketPPRTriggerCause cause =
        actionFilter.getCause(pollingLog, bitbucketAction, bitBucketHookEvent);

    assertEquals(
        hookEventAction,
        cause.getHookEvent(),
        "Bitbuckethook event and hockEvent property of cause object are the same.");
  }

  private BitBucketPPRPayload getCloudPayload(String res) {
    return getGenericPayload(res, BitBucketPPRCloudPayload.class);
  }

  private BitBucketPPRServerPayload getServerPayload(String res) {
    return (BitBucketPPRServerPayload) getGenericPayload(res, BitBucketPPRServerPayload.class);
  }

  private BitBucketPPRPayload getGenericPayload(String res, Class<?> payloadClass) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream(res);
    assertNotNull(is);
    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
    JsonReader reader = new JsonReader(isr);

    return new Gson().fromJson(reader, payloadClass);
  }

  private void runEnvironmentContributorForCause(BitBucketPPRTriggerCause cause)
      throws Exception {
    BitBucketPPREnvironmentContributor envContributor = new BitBucketPPREnvironmentContributor();

    Run run = mock(Run.class);

    List<Cause> causes = new ArrayList<>();
    causes.add(cause);
    when(run.getCauses()).thenReturn(causes);

    envContributor.buildEnvironmentFor(run, envVars, mock(TaskListener.class));
  }
}
