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

package io.jenkins.plugins.bitbucketpushandpullrequest.extension.dsl;

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRServerRepositoryPushActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import javaposse.jobdsl.plugin.RemovedJobAction;

import hudson.EnvVars;
import hudson.triggers.TriggerDescriptor;
import hudson.triggers.Trigger;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRHookJobDslExtensionTest {
  /* Global Jenkins instance mock */
  @Rule
  public JenkinsRule j = new JenkinsRule();

  @Test
  public void testDslTriggerPushAction() throws Exception {
    /* Create seed job which will process DSL */
    FreeStyleProject seedJob = j.createFreeStyleProject();
    ExecuteDslScripts dslScript = new ExecuteDslScripts();
    dslScript.setUseScriptText(Boolean.TRUE);
    dslScript.setScriptText("freeStyleJob('test-job') { triggers { bitbucketRepositoryPushAction(false, '') } }");
    dslScript.setTargets(null);
    dslScript.setIgnoreExisting(Boolean.FALSE);
    dslScript.setRemovedJobAction(RemovedJobAction.DELETE);
    seedJob.getBuildersList().add(dslScript);

    j.buildAndAssertSuccess(seedJob);

    /* Fetch the newly created job and check its trigger configuration */
    FreeStyleProject createdJob = (FreeStyleProject) j.getInstance().getItem("test-job");
    Map<TriggerDescriptor,Trigger<?>> triggers = createdJob.getTriggers();
    assertEquals(1, triggers.size());
    List<String> dispNames = new ArrayList<>();
    for (Map.Entry<TriggerDescriptor,Trigger<?> > entry : triggers.entrySet()) {
      //TriggerDescriptor key = entry.getKey();
      //String dispName = entry.getValue().getDescriptor().getDisplayName();
      BitBucketPPRTrigger.DescriptorImpl tmp = (BitBucketPPRTrigger.DescriptorImpl) entry.getValue().getDescriptor();
      BitBucketPPRTrigger tmp2 = (BitBucketPPRTrigger) entry.getValue();
      assertEquals(1, tmp2.getTriggers().size());
      String tmpNname = tmp2.getTriggers().get(0).getActionFilter().getClass().getName();
      //assertEquals(1, tmp.getTriggerDescriptors().size());
      //entry.getValue().getProjectActions();
      //tmp.getTriggers();
      String dispName = tmpNname.substring(tmpNname.lastIndexOf(".") + 1);
      dispNames.add(dispName);
    }
    assertEquals(1, dispNames.size());
    assertEquals(dispNames.get(0), "BitBucketPPRRepositoryPushActionFilter");
    //String dispName = createdJob.getBuilders().get(0).getDescriptor().getDisplayName();
    //String dispName = createdJob.getTrigger(BitBucketPPRRepositoryPushActionFilter.class).getDescriptor().getDisplayName();
    //assertEquals(dispName, "Bitbucket Cloud Push");
    /*BitBucketPPRTrigger trig = createdJob.getTrigger(BitBucketPPRTrigger.class);
    assertNotEquals(null, trig);
    List<?> t = trig.getTriggers();
    assertEquals(1, t.size());
    String tmp = t.get(0).getClass().getName();
    assertEquals(tmp.substring(tmp.lastIndexOf(".") + 1), "BitBucketPPRRepositoryPushActionFilter");*/
    
  }

  @Test
  public void testDslTriggerPRApproved() {
  }

  @Test
  public void testDslTriggerPRCreated() {
  }

  @Test
  public void testDslTriggerPRUpdated() {
  }

  @Test
  public void testDstTriggerAllPRActions() {
  }
}
