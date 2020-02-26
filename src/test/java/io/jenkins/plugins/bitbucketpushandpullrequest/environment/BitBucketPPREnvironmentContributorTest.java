/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2019, CloudBees, Inc.
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

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import hudson.EnvVars;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestServerAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.cloud.BitBucketPPRPullRequestCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.server.BitBucketPPRPullRequestServerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRServerRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerPayload;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPREnvironmentContributorTest {

  private EnvVars envVars;

  @Before
  public void buildEnvVarsSpy() {
    envVars = spy(EnvVars.class);
  }

  @Test
  public void buildEnvironmentForCloudRepoPushTest() {
    BitBucketPPRPayload payload = getCloudPayload("./cloud/repo_push.json");

    BitBucketPPRRepositoryCause cause = mock(BitBucketPPRRepositoryCause.class);
    when(cause.getRepositoryPayLoad()).thenReturn(new BitBucketPPRRepositoryAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "feature/do-not-merge"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.REPOSITORY_LINK,
        "https://bitbucket.org/some-repository/some-repo"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_REPOSITORY_URL,
            "https://bitbucket.org/some-repository/some-repo"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_REPOSITORY_UUID,
            "{6b5a1057-07ff-47c1-a65e-6c136cce4hj4}"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-van-me-nickname"));
  }

  @Test
  public void buildEnvironmentForCloudPullRequestCreatedTest() {
    BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_created.json");

    BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "feature/do-not-merge"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "I have to push the pram a lot X."));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, "Some description for PR"));
  }

  @Test
  public void buildEnvironmentForCloudPullRequestMergedTest() {
    BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_fulfilled.json");

    BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "feature/do-not-merge"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "I have to push the pram a lot X."));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, "Some description for PR"));
  }

  @Test
  public void buildEnvironmentForCloudPullRequestUpdatedTest() {
    BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_updated.json");

    BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "feature/do-not-merge"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "I have to push the pram a lot X."));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, "Some description for PR"));
  }

  @Test
  public void buildEnvironmentForCloudPullRequestApprovedTest() {
    BitBucketPPRPayload payload = getCloudPayload("./cloud/pr_approved.json");

    BitBucketPPRPullRequestCause cause = mock(BitBucketPPRPullRequestCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "feature/do-not-merge"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "develop"));
    assertThat(envVars,
        hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
            "https://bitbucket.org/some-repo-namespace/some-repo/pull-requests/198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "198"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-nickname"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "I have to push the pram a lot X."));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, "Some description for PR"));
  }

  @Test
  public void buildEnvironmentForServerPullRequestOpenedTest() {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_opened.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestServerAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "61"));
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
        "http://example.org/projects/ABC/repos/some-repo/pull-requests/61"
    ));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE,
        "test"
    ));
    assertThat(envVars, hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION,
        ""
    ));
  }

  @Test
  public void buildEnvironmentForServerPullRequestModifiedTest() {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_modified.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestServerAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
      "http://example.org/projects/ABC/repos/some-repo/pull-requests/13"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "13"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
  }

  @Test
  public void buildEnvironmentForServerPullRequestApprovedTest() {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_reviewer_approved.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestServerAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
      "http://example.org/projects/ABC/repos/some-repo/pull-requests/12"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "12"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "some-reviewer"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
  }

  @Test
  public void buildEnvironmentForServerPullRequestMergedTest() {
    BitBucketPPRServerPayload payload = getServerPayload("./server/pr_merged.json");

    BitBucketPPRPullRequestServerCause cause = mock(BitBucketPPRPullRequestServerCause.class);
    when(cause.getPullRequestPayLoad()).thenReturn(new BitBucketPPRPullRequestServerAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "develop"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_TARGET_BRANCH, "master"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_LINK,
      "http://example.org/projects/ABC/repos/some-repo/pull-requests/61"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_ID, "61"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_TITLE, "test"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_PULL_REQUEST_DESCRIPTION, ""));
  }

  @Test
  public void buildEnvironmentForServerRepoPushTest() {
    BitBucketPPRServerPayload payload = getServerPayload("./server/repo_refs_changed.json");

    BitBucketPPRServerRepositoryCause cause = mock(BitBucketPPRServerRepositoryCause.class);
    when(cause.getServerRepositoryPayLoad()).thenReturn(new BitBucketPPRServerRepositoryAction(payload));

    // do
    runEnvironmentContributorForCause(cause);

    // assert
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.REPOSITORY_NAME, "some-repo"));
    assertThat(envVars, hasEntry(BitBucketPPREnvironmentContributor.BITBUCKET_ACTOR, "me-name"));
  }

  private BitBucketPPRPayload getCloudPayload(String res) {
    return (BitBucketPPRPayload) getGenericPayload(
      res, BitBucketPPRNewPayload.class
    );
  }

  private BitBucketPPRServerPayload getServerPayload(String res) {
    return (BitBucketPPRServerPayload) getGenericPayload(
      res, BitBucketPPRServerPayload.class
    );
  }

  private BitBucketPPRPayload getGenericPayload(String res, Class<?> payloadClass) {
    JsonReader reader = null;
    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream(res);
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to parse JSON payload stub.");
    }

    return new Gson().fromJson(reader, payloadClass);
  }

  private void runEnvironmentContributorForCause(BitBucketPPRTriggerCause cause) {
    BitBucketPPREnvironmentContributor envContributor = new BitBucketPPREnvironmentContributor();

    Run run = mock(Run.class);

    List<Cause> causes = new ArrayList<>();
    causes.add(cause);
    when(run.getCauses()).thenReturn(causes);

    try {
      envContributor.buildEnvironmentFor(run, envVars, mock(TaskListener.class));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception thrown");
    }
  }
}
