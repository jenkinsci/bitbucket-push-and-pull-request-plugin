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

package io.jenkins.plugins.bitbucketpushandpullrequest.extension.dsl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.model.FreeStyleProject;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestDeclinedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestMergedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import javaposse.jobdsl.plugin.RemovedJobAction;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRHookJobDslExtensionTest {
  /* Global Jenkins instance mock */
  @Rule
  public JenkinsRule j = new JenkinsRule();

  private void createSeedJob(String desc) throws Exception {
    /* Create seed job which will process DSL */
    FreeStyleProject seedJob = j.createFreeStyleProject();
    ExecuteDslScripts dslScript = new ExecuteDslScripts();
    dslScript.setUseScriptText(Boolean.TRUE);
    dslScript.setScriptText(desc);
    dslScript.setTargets(null);
    dslScript.setIgnoreExisting(Boolean.FALSE);
    dslScript.setRemovedJobAction(RemovedJobAction.DELETE);
    seedJob.getBuildersList().add(dslScript);

    j.buildAndAssertSuccess(seedJob);
  }

  private String readDslScript(String path) {
    String script = null;
    try {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(path);
        script = IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return script;
  }

  @Test
  public void testDslTriggerPushActionFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPushActionFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      BitBucketPPRRepositoryPushActionFilter tmp3 = (BitBucketPPRRepositoryPushActionFilter) tmp2.getTriggers().get(0)
          .getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRRepositoryPushActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }

  @Test
  public void testDslTriggerIsToApprovePushActionFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerIsToApprovePushActionFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      BitBucketPPRRepositoryPushActionFilter tmp3 =  (BitBucketPPRRepositoryPushActionFilter) tmp2.getTriggers().get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRRepositoryPushActionFilter", dispNames.get(0));
    assertTrue(isToApprove);
  }

  @Test
  public void testDslTriggerPRApprovedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRApprovedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestApprovedActionFilter tmp3 = (BitBucketPPRPullRequestApprovedActionFilter) tmp2
          .getTriggers().get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }

  @Test
  @Ignore
  public void testDslTriggerPRIsToApproveApprovedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRIsToApproveApprovedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestApprovedActionFilter tmp3 = (BitBucketPPRPullRequestApprovedActionFilter) tmp2.getTriggers().get(0)
          .getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(0));
    assertTrue(isToApprove);
  }

  @Test
  public void testDslTriggerPRAllowedBranchesApprovedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesApprovedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRCommentCreatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRCommentCreatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentCreatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRAllowedBranchesCommentCreatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesCommentCreatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentCreatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRCommentFilterCommentCreatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRCommentFilterCommentCreatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentCreatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRCommentUpdatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRCommentUpdatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentUpdatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRAllowedBranchesCommentUpdatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesCommentUpdatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentUpdatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRCommentFilterCommentUpdatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRCommentFilterCommentUpdatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentUpdatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRCommentDeletedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRCommentDeletedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentDeletedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRAllowedBranchesCommentDeletedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesCommentDeletedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCommentDeletedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRCreatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRCreatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRAllowBranchesCreatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowBranchesCreatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestCreatedActionFilter tmp3 = (BitBucketPPRPullRequestCreatedActionFilter) tmp2.getTriggers()
          .get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }

  @Test
  public void testDslTriggerPRAllowBranchesWithApproveCreatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowBranchesWithApproveCreatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestCreatedActionFilter tmp3 = (BitBucketPPRPullRequestCreatedActionFilter) tmp2
          .getTriggers().get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));
    assertTrue(isToApprove);
  }

  @Test
  public void testDslTriggerPRUpdatedFreeeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRUpdatedFreeeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRAllowedBranchesUpdatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesUpdatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestUpdatedActionFilter tmp3 = (BitBucketPPRPullRequestUpdatedActionFilter) tmp2.getTriggers()
          .get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }

  @Test
  public void testDslTriggerPRAllowedBranchesWithApproveUpdatedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesWithApproveUpdatedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestUpdatedActionFilter tmp3 = (BitBucketPPRPullRequestUpdatedActionFilter) tmp2.getTriggers()
          .get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(0));
    assertTrue(isToApprove);
  }


  @Test
  public void testDslTriggerPRMergedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRMergedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestMergedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRDeclinedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRDeclinedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestDeclinedActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslTriggerPRAllowedBranchesMergedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesMergedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestMergedActionFilter tmp3 = (BitBucketPPRPullRequestMergedActionFilter) tmp2.getTriggers()
          .get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestMergedActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }

  @Test
  public void testDslTriggerPRAllowedBranchesDeclinedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesDeclinedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestDeclinedActionFilter tmp3 = (BitBucketPPRPullRequestDeclinedActionFilter) tmp2.getTriggers()
              .get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestDeclinedActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }

  @Test
  public void testDslTriggerPRAllowedBranchesWithApproveMergedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPRAllowedBranchesWithApproveMergedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      BitBucketPPRPullRequestMergedActionFilter tmp3 = (BitBucketPPRPullRequestMergedActionFilter) tmp2.getTriggers()
          .get(0).getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestMergedActionFilter", dispNames.get(0));
    assertTrue(isToApprove);
  }

  @Test
  public void testDslTriggerCreateUpdatedApprovedPRActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerCreateUpdatedApprovedPRActionsFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Three different triggers expected */
      assertEquals(3, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(3, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(2));
  }

  @Test
  public void testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerCreateUpdatedMergedDeclinedApprovedPRAllowBranchesActionsFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Five different triggers expected */
      assertEquals(5, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(5, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestMergedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestDeclinedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(4));
  }

  @Test
  public void testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesWithApproveActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesWithApproveActionsFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Three different triggers expected */
      assertEquals(4, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(4, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestMergedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(3));
  }

  @Test
  public void testDslMultipleJobsInSeedFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslMultipleJobsInSeedFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job1");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));

    /* Verify second job content */
    createdJob = (FreeStyleProject) j.getInstance().getItem("test-job2");
    /* Go through all triggers to validate DSL */
    triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    dispNames.clear();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRRepositoryPushActionFilter", dispNames.get(0));
  }

  @Test
  public void testDslServerAllPRActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRActionsFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Five different triggers expected */
      assertEquals(5, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(5, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerDeclinedActionFilter", dispNames.get(4));
  }

  @Test
  public void testDslServerAllPRAllowedBranchesActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRAllowedBranchesActionsFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Six different triggers expected */
      assertEquals(6, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(5).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(6, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(4));
    assertEquals("BitBucketPPRPullRequestServerDeclinedActionFilter", dispNames.get(5));
  }

  @Test
  public void testDslServerAllPRAllowedBranchesWithApproveActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRAllowedBranchesWithApproveActionsFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Four different triggers expected */
      assertEquals(5, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(5, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(4));
  }

  @Test
  public void testDslServerAllPRUnfilteredAndAllowedBranchesActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRUnfilteredAndAllowedBranchesActionsFreeStyle.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Twelve different triggers expected */
      assertEquals(12, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(5).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(6).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(7).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(8).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(9).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(10).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(11).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(12, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(4));
    assertEquals("BitBucketPPRPullRequestServerDeclinedActionFilter", dispNames.get(5));

    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(6));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(7));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(8));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(9));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(10));
    assertEquals("BitBucketPPRPullRequestServerDeclinedActionFilter", dispNames.get(11));
  }

  @Test
  public void testDslServerAllPRActionsPipeline() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRActionsPipeline.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    PipelineTriggersJobProperty pipelineTriggers = createdJob.getProperty(PipelineTriggersJobProperty.class);
    Map<TriggerDescriptor, Trigger<?>> triggers = pipelineTriggers.getTriggersMap();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Five different triggers expected */
      assertEquals(5, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(5, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerDeclinedActionFilter", dispNames.get(4));
  }

  @Test
  public void testDslServerAllPRAllowedBranchesActionsPipeline() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRAllowedBranchesActionsPipeline.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    PipelineTriggersJobProperty pipelineTriggers = createdJob.getProperty(PipelineTriggersJobProperty.class);
    Map<TriggerDescriptor, Trigger<?>> triggers = pipelineTriggers.getTriggersMap();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Six different triggers expected */
      assertEquals(6, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(5).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(6, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(4));
    assertEquals("BitBucketPPRPullRequestServerDeclinedActionFilter", dispNames.get(5));
  }

  @Ignore
  @Test
  public void testDslServerAllPRAllowedBranchesWithApproveActionsPipeline() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRAllowedBranchesWithApproveActionsPipeline.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    PipelineTriggersJobProperty pipelineTriggers = createdJob.getProperty(PipelineTriggersJobProperty.class);
    Map<TriggerDescriptor, Trigger<?>> triggers = pipelineTriggers.getTriggersMap();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Four different triggers expected */
      assertEquals(5, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(5, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(4));
  }

  @Test
  public void testDslServerAllPRUnfilteredAndAllowedBranchesActionsPipeline() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslServerAllPRUnfilteredAndAllowedBranchesActionsPipeline.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    PipelineTriggersJobProperty pipelineTriggers = createdJob.getProperty(PipelineTriggersJobProperty.class);
    Map<TriggerDescriptor, Trigger<?>> triggers = pipelineTriggers.getTriggersMap();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Eleven different triggers expected */
      assertEquals(11, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(4).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(5).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(6).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(7).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(8).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(9).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(10).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(11, dispNames.size());
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(3));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(4));
    assertEquals("BitBucketPPRPullRequestServerCreatedActionFilter", dispNames.get(5));
    assertEquals("BitBucketPPRPullRequestServerUpdatedActionFilter", dispNames.get(6));
    assertEquals("BitBucketPPRPullRequestServerSourceUpdatedActionFilter", dispNames.get(7));
    assertEquals("BitBucketPPRPullRequestServerApprovedActionFilter", dispNames.get(8));
    assertEquals("BitBucketPPRPullRequestServerMergedActionFilter", dispNames.get(9));
    assertEquals("BitBucketPPRPullRequestServerDeclinedActionFilter", dispNames.get(10));
  }

  @Ignore
  @Test
  public void testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesWithApproveActionsPipeline() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesWithApproveActionsPipeline.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    PipelineTriggersJobProperty pipelineTriggers = createdJob.getProperty(PipelineTriggersJobProperty.class);
    Map<TriggerDescriptor, Trigger<?>> triggers = pipelineTriggers.getTriggersMap();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* Three different triggers expected */
      assertEquals(4, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(2).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpNname = tmp2.getTriggers().get(3).getActionFilter().getClass().getName();
      dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(4, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(1));
    assertEquals("BitBucketPPRPullRequestMergedActionFilter", dispNames.get(2));
    assertEquals("BitBucketPPRPullRequestApprovedActionFilter", dispNames.get(3));
  }

  @Test
  public void testDslTriggerDeclinedPRAllowBranchesPipeline() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerDeclinedPRAllowBranchesPipeline.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    PipelineTriggersJobProperty pipelineTriggers = createdJob.getProperty(PipelineTriggersJobProperty.class);
    Map<TriggerDescriptor, Trigger<?>> triggers = pipelineTriggers.getTriggersMap();
    /* Only one 'triggers{}' closure */
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      /* One triggers expected */
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      assertEquals("BitBucketPPRPullRequestDeclinedActionFilter", dispName);

      BitBucketPPRPullRequestDeclinedActionFilter actionFilter = (BitBucketPPRPullRequestDeclinedActionFilter) tmp2.getTriggers().get(0).getActionFilter();
      assertEquals("**", actionFilter.allowedBranches);
    }
  }

  @Test
  public void testDslTriggerPushActionPipeline() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerPushActionPipeline.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    WorkflowJob createdJob = (WorkflowJob) j.getInstance().getItem("test-job");
    /* Go through all triggers to validate DSL */
    PipelineTriggersJobProperty pipelineTriggers = createdJob.getProperty(PipelineTriggersJobProperty.class);
    Map<TriggerDescriptor, Trigger<?>> triggers = pipelineTriggers.getTriggersMap();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    boolean isToApprove = false;
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);

      BitBucketPPRRepositoryPushActionFilter tmp3 = (BitBucketPPRRepositoryPushActionFilter) tmp2.getTriggers().get(0)
          .getActionFilter();
      isToApprove = tmp3.shouldSendApprove();
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRRepositoryPushActionFilter", dispNames.get(0));
    assertFalse(isToApprove);
  }

  @Test
  public void testDslMultipleJobsInSeedVarious() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslMultipleJobsInSeedVarious.groovy"));
    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job1");
    WorkflowJob createdPipelineJob;
    PipelineTriggersJobProperty pipelineTriggers;
    /* Go through all triggers to validate DSL */
    Map<TriggerDescriptor, Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestCreatedActionFilter", dispNames.get(0));

    /* Verify second job content */
    createdJob = (FreeStyleProject) j.getInstance().getItem("test-job2");
    /* Go through all triggers to validate DSL */
    triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    dispNames.clear();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals("BitBucketPPRPullRequestUpdatedActionFilter", dispNames.get(0));

    /* Verify third job content */
    createdPipelineJob = (WorkflowJob) j.getInstance().getItem("test-job3");
    /* Go through all triggers to validate DSL */
    pipelineTriggers = createdPipelineJob.getProperty(PipelineTriggersJobProperty.class);
    triggers = pipelineTriggers.getTriggersMap();
    assertEquals(1, triggers.size());
    dispNames.clear();
    for (Trigger<?> entry : triggers.values()) {
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry;
      assertEquals(2, tmp2.getTriggers().size());
      String tmpName = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      String dispName = tmpName.substring(tmpName.lastIndexOf(".") + 1);
      dispNames.add(dispName);
      tmpName = tmp2.getTriggers().get(1).getActionFilter().getClass().getName();
      dispName = tmpName.substring(tmpName.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(2, dispNames.size());
    assertEquals("BitBucketPPRRepositoryPushActionFilter", dispNames.get(0));
    assertEquals("BitBucketPPRPullRequestCommentDeletedActionFilter", dispNames.get(1));
  }
}
