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
import static org.junit.Assert.assertEquals;
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

  @Test
  public void testDslTriggerPushAction() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { repositoryPushAction(false, false, '') } } }");
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
    assertEquals(dispNames.get(0), "BitBucketPPRRepositoryPushActionFilter");
  }

  @Test
  public void testDslTriggerPRApproved() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestApprovedAction(false) } } }");
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
  public void testDslTriggerPRAllowedBranchesApproved() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestApprovedAction(false, \"**\") } } }");
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
  public void testDslTriggerPRCommentCreated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentCreatedAction() } } }");
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
  public void testDslTriggerPRAllowedBranchesCommentCreated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentCreatedAction(\"**\") } } }");
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
  public void testDslTriggerPRCommentFilterCommentCreated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentCreatedAction(\"**\", \"text\") } } }");
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
  public void testDslTriggerPRCommentUpdated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentUpdatedAction() } } }");
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
  public void testDslTriggerPRAllowedBranchesCommentUpdated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentUpdatedAction(\"**\") } } }");
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
  public void testDslTriggerPRCommentFilterCommentUpdated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentUpdatedAction(\"**\", \"text\")}}}");
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
  public void testDslTriggerPRCommentDeleted() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentDeletedAction() } } }");
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
  public void testDslTriggerPRAllowedBranchesCommentDeleted() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCommentDeletedAction(\"**\") } } }");
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
  public void testDslTriggerPRCreated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCreatedAction() } } }");
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
  public void testDslTriggerPRAllowBranchesCreated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCreatedAction(\"**\") } } }");
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
  public void testDslTriggerPRAllowBranchesWithApproveCreated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCreatedAction(\"**\", true) } } }");
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
  public void testDslTriggerPRUpdated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestUpdatedAction() } } }");
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
  public void testDslTriggerPRAllowedBranchesUpdated() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestUpdatedAction(\"**\") } } }");
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
  public void testDslTriggerPRMerged() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestMergedAction() } } }");
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
  public void testDslTriggerPRAllowedBranchesMerged() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestMergedAction(\"**\") } } }");
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
  public void testDslTriggerCreateUpdatedApprovedPRActions() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCreatedAction()\npullRequestUpdatedAction()\npullRequestApprovedAction(false) } } }");
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
  public void testDslTriggerCreateUpdatedMergedApprovedPRAllowBranchesActions() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job') { triggers { bitbucketTriggers { pullRequestCreatedAction(\"**\")\npullRequestUpdatedAction(\"**\")\npullRequestMergedAction(\"**\")\npullRequestApprovedAction(false, \"**\") } } }");
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
  public void testDslMultipleJobsInSeed() throws Exception {
    /* Create seed job which will process DSL */
    createSeedJob(
        "freeStyleJob('test-job1') { triggers { bitbucketTriggers { pullRequestCreatedAction() } } };freeStyleJob('test-job2') { triggers { bitbucketTriggers { repositoryPushAction(false, false, '') } } }");
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
  public void testDslServerAllPRActions() throws Exception {
    String seedJobDesc = "freeStyleJob('test-job') { triggers { bitbucketTriggers {";
    seedJobDesc += "pullRequestServerCreatedAction()\n";
    seedJobDesc += "pullRequestServerUpdatedAction()\n";
    seedJobDesc += "pullRequestServerApprovedAction(false)\n";
    seedJobDesc += "pullRequestServerMergedAction()";
    seedJobDesc += "} } }";
    /* Create seed job which will process DSL */
    createSeedJob(seedJobDesc);
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
  public void testDslServerAllPRAllowedBranchesActions() throws Exception {
    String seedJobDesc = "freeStyleJob('test-job') { triggers { bitbucketTriggers {";
    seedJobDesc += "pullRequestServerCreatedAction(\"*\")\n";
    seedJobDesc += "pullRequestServerUpdatedAction(\"*\")\n";
    seedJobDesc += "pullRequestServerSourceUpdatedAction(\"*\")\n";
    seedJobDesc += "pullRequestServerApprovedAction(false, \"*\")\n";
    seedJobDesc += "pullRequestServerMergedAction(\"*\")";
    seedJobDesc += "} } }";
    /* Create seed job which will process DSL */
    createSeedJob(seedJobDesc);
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
  public void testDslServerAllPRUnfilteredAndAllowedBranchesActions() throws Exception {
    String seedJobDesc = "freeStyleJob('test-job') { triggers { bitbucketTriggers {";
    seedJobDesc += "pullRequestServerCreatedAction()\n";
    seedJobDesc += "pullRequestServerUpdatedAction()\n";
    seedJobDesc += "pullRequestServerSourceUpdatedAction()\n";
    seedJobDesc += "pullRequestServerApprovedAction(false)\n";
    seedJobDesc += "pullRequestServerMergedAction()\n";

    seedJobDesc += "pullRequestServerCreatedAction(\"*\")\n";
    seedJobDesc += "pullRequestServerUpdatedAction(\"*\")\n";
    seedJobDesc += "pullRequestServerSourceUpdatedAction(\"*\")\n";
    seedJobDesc += "pullRequestServerApprovedAction(false, \"*\")\n";
    seedJobDesc += "pullRequestServerMergedAction(\"*\")";
    seedJobDesc += "} } }";
    /* Create seed job which will process DSL */
    createSeedJob(seedJobDesc);
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
