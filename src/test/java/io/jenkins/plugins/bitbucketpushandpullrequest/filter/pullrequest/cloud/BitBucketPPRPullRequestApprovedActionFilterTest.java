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

package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.EnvVars;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@WithJenkins
class BitBucketPPRPullRequestApprovedActionFilterTest {

  private JenkinsRule j;

  @BeforeEach
  void setUp(JenkinsRule rule) {
    j = rule;
  }

  private void createSeedJob(String script) throws Exception {
    WorkflowJob pipelineJob = j.createProject(WorkflowJob.class, "pipelineJob");

    // Set the pipeline script (like setting a Jenkinsfile)
    pipelineJob.setDefinition(new CpsFlowDefinition(script, true));

    // Build and assert that it succeeded
    j.buildAndAssertSuccess(pipelineJob);
  }

  @Test
  void testMatches() {
    String allowedBranches = "master";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/master", null));
    assertFalse(c.matches(allowedBranches, "origin/something/master", null));
    assertTrue(c.matches(allowedBranches, "master", null));
    assertFalse(c.matches(allowedBranches, "dev", null));

    allowedBranches = "origin/*/dev";

    assertFalse(c.matches(allowedBranches, "origintestdev", null));
    assertTrue(c.matches(allowedBranches, "origin/test/dev", null));
    assertFalse(c.matches(allowedBranches, "origin/test/release", null));
    assertFalse(c.matches(allowedBranches, "origin/test/something/release", null));

    allowedBranches = "origin/*";

    assertTrue(c.matches(allowedBranches, "origin/master", null));

    allowedBranches = "**/magnayn/*";

    assertTrue(c.matches(allowedBranches, "origin/magnayn/b1", null));
    assertTrue(c.matches(allowedBranches, "remote/origin/magnayn/b1", null));
    assertTrue(c.matches(allowedBranches, "remotes/origin/magnayn/b1", null));

    allowedBranches = ("*/my.branch/*");

    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", null));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", null));
    assertFalse(c.matches(allowedBranches, "remote/origin/my.branch/b1", null));
    assertTrue(c.matches(allowedBranches, "remotes/origin/my.branch/b1", null));

    allowedBranches = "**";

    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", null));
    assertTrue(c.matches(allowedBranches, "origin/my-branch/b1", null));
    assertTrue(c.matches(allowedBranches, "remote/origin/my.branch/b1", null));
    assertTrue(c.matches(allowedBranches, "remotes/origin/my.branch/b1", null));

    allowedBranches = "*";

    assertTrue(c.matches(allowedBranches, "origin/x", null));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", null));
  }

  @Test
  void testMatchEnv() {
    HashMap<String, String> envMap = new HashMap<>();
    envMap.put("master", "master");
    envMap.put("origin", "origin");
    envMap.put("dev", "dev");
    envMap.put("magnayn", "magnayn");
    envMap.put("mybranch", "my.branch");
    envMap.put("anyLong", "**");
    envMap.put("anyShort", "*");
    envMap.put("anyEmpty", "");
    EnvVars env = new EnvVars(envMap);

    String allowedBranches = "${master}";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/master", env));
    assertFalse(c.matches(allowedBranches, "origin/something/master", env));
    assertTrue(c.matches(allowedBranches, "master", env));
    assertFalse(c.matches(allowedBranches, "dev", env));

    allowedBranches = "${origin}/*/${dev}";

    assertFalse(c.matches(allowedBranches, "origintestdev", env));
    assertTrue(c.matches(allowedBranches, "origin/test/dev", env));
    assertFalse(c.matches(allowedBranches, "origin/test/release", env));
    assertFalse(c.matches(allowedBranches, "origin/test/something/release", env));

    allowedBranches = "${origin}/*";

    assertTrue(c.matches(allowedBranches, "origin/master", env));

    allowedBranches = "**/${magnayn}/*";

    assertTrue(c.matches(allowedBranches, "origin/magnayn/b1", env));
    assertTrue(c.matches(allowedBranches, "remote/origin/magnayn/b1", env));

    allowedBranches = "*/${mybranch}/*";

    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", env));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", env));
    assertFalse(c.matches(allowedBranches, "remote/origin/my.branch/b1", env));

    allowedBranches = "${anyLong}";

    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", env));
    assertTrue(c.matches(allowedBranches, "origin/my-branch/b1", env));
    assertTrue(c.matches(allowedBranches, "remote/origin/my.branch/b1", env));

    allowedBranches = "${anyShort}";

    assertTrue(c.matches(allowedBranches, "origin/x", env));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", env));

    allowedBranches = "${anyEmpty}";

    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", env));
    assertTrue(c.matches(allowedBranches, "origin/my-branch/b1", env));
    assertTrue(c.matches(allowedBranches, "remote/origin/my.branch/b1", env));
  }

  @Test
  void testUsesRefsHeads() {
    String allowedBranches = "refs/heads/j*n*";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertTrue(c.matches(allowedBranches, "refs/heads/jenkins", null));
    assertTrue(c.matches(allowedBranches, "refs/heads/jane", null));
    assertTrue(c.matches(allowedBranches, "refs/heads/jones", null));
    assertFalse(c.matches(allowedBranches, "origin/jenkins", null));
    assertFalse(c.matches(allowedBranches, "remote/origin/jane", null));
  }

  @Test
  void testUsesJavaPatternDirectlyIfPrefixedWithColon() {

    String allowedBranches = ":^(?!(origin/prefix)).*";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin", null));
    assertTrue(c.matches(allowedBranches, "origin/master", null));
    assertTrue(c.matches(allowedBranches, "origin/feature", null));
    assertFalse(c.matches(allowedBranches, "origin/prefix_123", null));
    assertFalse(c.matches(allowedBranches, "origin/prefix", null));
    assertFalse(c.matches(allowedBranches, "origin/prefix-abc", null));
  }

  @Test
  void testMatchesNot1() {
    String allowedBranches = "*/master";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertFalse(c.matches(allowedBranches, "master", null));
  }

  @Test
  void testMatchesNot2() {
    String allowedBranches = "develop, :^(?!master$).*";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertFalse(c.matches(allowedBranches, "master", null));

    allowedBranches = ":^(?!develop$).*";
    assertFalse(c.matches(allowedBranches, "develop", null));
  }

  @Test
  void testMatchesEmptyBranches() {

    String allowedBranches = "";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertTrue(c.matches(allowedBranches, "master", null));
    assertTrue(c.matches(allowedBranches, "develop", null));
    assertTrue(c.matches(allowedBranches, "feature/new-stuff", null));
  }

  @Test
  void testUsesJavaPatternWithRepetition() {
    String allowedBranches = ":origin/release-\\d{8}";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/release-20150101", null));
    assertFalse(c.matches(allowedBranches, "origin/release-2015010", null));
    assertFalse(c.matches(allowedBranches, "origin/release-201501011", null));
    assertFalse(c.matches(allowedBranches, "origin/release-20150101-something", null));
  }

  @Test
  void testUsesJavaPatternToExcludeMultipleBranches() {
    String allowedBranches = ":^(?!origin/master$|origin/develop$).*";

    BitBucketPPRPullRequestApprovedActionFilter c =
        new BitBucketPPRPullRequestApprovedActionFilter(false);
    c.setAllowedBranches(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/branch1", null));
    assertTrue(c.matches(allowedBranches, "origin/branch-2", null));
    assertTrue(c.matches(allowedBranches, "origin/master123", null));
    assertTrue(c.matches(allowedBranches, "origin/develop-123", null));
    assertFalse(c.matches(allowedBranches, "origin/master", null));
    assertFalse(c.matches(allowedBranches, "origin/develop", null));
  }

  private String readScript(String path) throws Exception {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream(path);
    assertNotNull(is);
    return IOUtils.toString(is, StandardCharsets.UTF_8);
  }

  @Test
  void testPipelineTrigger() throws Exception {
    createSeedJob(
        readScript("./pipelines/testPipelineCloudTriggerPullRequestApprovedActionFilter"));
    // get newly created pipeline job
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("pipelineJob");
    assert createdJob != null;
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpName = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpName.substring(tmpName.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestApprovedActionFilter tmp3 =
          (BitBucketPPRPullRequestApprovedActionFilter) tmp2.getTriggers().get(0)
              .getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }

    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }
}
