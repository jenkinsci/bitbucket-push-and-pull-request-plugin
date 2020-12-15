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

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestApprovedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestCreatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestMergedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud.BitBucketPPRPullRequestUpdatedActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import javaposse.jobdsl.plugin.RemovedJobAction;
import hudson.triggers.TriggerDescriptor;
import hudson.triggers.Trigger;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty;


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

  private String readDslScript(String path) throws Exception {
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
    PipelineTriggersJobProperty pipelineTriggers = (PipelineTriggersJobProperty) createdJob.getProperty(PipelineTriggersJobProperty.class);
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
    assertEquals(dispNames.get(0), "BitBucketPPRRepositoryPushActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRRepositoryPushActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestApprovedActionFilter");
    assertFalse(isToApprove);
  }

  @Test
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestApprovedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestApprovedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentCreatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentCreatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentCreatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentUpdatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentUpdatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentUpdatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentDeletedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCommentDeletedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCreatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCreatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCreatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestUpdatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestUpdatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestUpdatedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestMergedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestMergedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestMergedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCreatedActionFilter");
    assertEquals(dispNames.get(1), "BitBucketPPRPullRequestUpdatedActionFilter");
    assertEquals(dispNames.get(2), "BitBucketPPRPullRequestApprovedActionFilter");
  }

  @Test
  public void testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesActionsFreeStyle() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(readDslScript("./dsl/testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesActionsFreeStyle.groovy"));
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCreatedActionFilter");
    assertEquals(dispNames.get(1), "BitBucketPPRPullRequestUpdatedActionFilter");
    assertEquals(dispNames.get(2), "BitBucketPPRPullRequestMergedActionFilter");
    assertEquals(dispNames.get(3), "BitBucketPPRPullRequestApprovedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCreatedActionFilter");
    assertEquals(dispNames.get(1), "BitBucketPPRPullRequestUpdatedActionFilter");
    assertEquals(dispNames.get(2), "BitBucketPPRPullRequestMergedActionFilter");
    assertEquals(dispNames.get(3), "BitBucketPPRPullRequestApprovedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestCreatedActionFilter");

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
    assertEquals(dispNames.get(0), "BitBucketPPRRepositoryPushActionFilter");
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
      /* Four different triggers expected */
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestServerCreatedActionFilter");
    assertEquals(dispNames.get(1), "BitBucketPPRPullRequestServerUpdatedActionFilter");
    assertEquals(dispNames.get(2), "BitBucketPPRPullRequestServerApprovedActionFilter");
    assertEquals(dispNames.get(3), "BitBucketPPRPullRequestServerMergedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestServerCreatedActionFilter");
    assertEquals(dispNames.get(1), "BitBucketPPRPullRequestServerUpdatedActionFilter");
    assertEquals(dispNames.get(2), "BitBucketPPRPullRequestServerSourceUpdatedActionFilter");
    assertEquals(dispNames.get(3), "BitBucketPPRPullRequestServerApprovedActionFilter");
    assertEquals(dispNames.get(4), "BitBucketPPRPullRequestServerMergedActionFilter");
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
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestServerCreatedActionFilter");
    assertEquals(dispNames.get(1), "BitBucketPPRPullRequestServerUpdatedActionFilter");
    assertEquals(dispNames.get(2), "BitBucketPPRPullRequestServerSourceUpdatedActionFilter");
    assertEquals(dispNames.get(3), "BitBucketPPRPullRequestServerApprovedActionFilter");
    assertEquals(dispNames.get(4), "BitBucketPPRPullRequestServerMergedActionFilter");
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
      /* Four different triggers expected */
      assertEquals(10, tmp2.getTriggers().size());
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
    }
    assertEquals(10, dispNames.size());
    assertEquals(dispNames.get(0), "BitBucketPPRPullRequestServerCreatedActionFilter");
    assertEquals(dispNames.get(1), "BitBucketPPRPullRequestServerUpdatedActionFilter");
    assertEquals(dispNames.get(2), "BitBucketPPRPullRequestServerSourceUpdatedActionFilter");
    assertEquals(dispNames.get(3), "BitBucketPPRPullRequestServerApprovedActionFilter");
    assertEquals(dispNames.get(4), "BitBucketPPRPullRequestServerMergedActionFilter");
    assertEquals(dispNames.get(5), "BitBucketPPRPullRequestServerCreatedActionFilter");
    assertEquals(dispNames.get(6), "BitBucketPPRPullRequestServerUpdatedActionFilter");
    assertEquals(dispNames.get(7), "BitBucketPPRPullRequestServerSourceUpdatedActionFilter");
    assertEquals(dispNames.get(8), "BitBucketPPRPullRequestServerApprovedActionFilter");
    assertEquals(dispNames.get(9), "BitBucketPPRPullRequestServerMergedActionFilter");
  }
}
