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
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.EnvVars;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.repository.BitBucketPPRRepositoryCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPREnvironmentContributorTest {
  @Mock
  private BitBucketPPRJobProbe probe;

  @Test
  public void buildEnvironmentForCloudRepoPushTest() {

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
    BitBucketPPRRepositoryAction action = new BitBucketPPRRepositoryAction(payload);

    BitBucketPPREnvironmentContributor envContributor = new BitBucketPPREnvironmentContributor();
    Run run = mock(Run.class);
    EnvVars envVars = spy(EnvVars.class);
    TaskListener taskListener = mock(TaskListener.class);
    BitBucketPPRRepositoryCause cause = mock(BitBucketPPRRepositoryCause.class);

    List<Cause> causes = new ArrayList<>();
    causes.add(cause);
    when(run.getCauses()).thenReturn(causes);
    when(cause.getRepositoryPayLoad()).thenReturn(action);

    try {
      envContributor.buildEnvironmentFor(run, envVars, taskListener);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Excetion thrown");
    }

    assertThat(envVars, IsMapContaining.hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_SOURCE_BRANCH, "feature/do-not-merge"));
    assertThat(envVars, IsMapContaining.hasEntry(
        BitBucketPPREnvironmentContributor.REPOSITORY_LINK, "https://bitbucket.org/some-repository/some-repo"));
    assertThat(envVars, IsMapContaining.hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_REPOSITORY_URL, "https://bitbucket.org/some-repository/some-repo"));
    assertThat(envVars, IsMapContaining.hasEntry(
        BitBucketPPREnvironmentContributor.BITBUCKET_REPOSITORY_UUID, "{6b5a1057-07ff-47c1-a65e-6c136cce4hj4}"));
  }
}
