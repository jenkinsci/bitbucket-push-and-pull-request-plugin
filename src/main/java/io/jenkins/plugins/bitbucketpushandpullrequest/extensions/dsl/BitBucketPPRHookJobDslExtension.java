/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
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


package io.jenkins.plugins.bitbucketpushandpullrequest.extensions.dsl;

import hudson.Extension;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

@Extension(optional = true)
public class BitBucketPPRHookJobDslExtension extends ContextExtensionPoint {

  /**
   * Provides wrapper for multiple triggers when defining job-dsl.
   * This {@link DslExtensionMethod} is only valid for Freestyle jobs
   * since this function (<a href="https://github.com/jenkinsci/job-dsl-plugin/blob/job-dsl-1.77/job-dsl-core/src/main/groovy/javaposse/jobdsl/dsl/Project.groovy#L161">see</a>) gets invoked when processing freestyle dsl.
   * The triggers context has been deprecated for Pipeline jobs, <a href="https://github.com/jenkinsci/job-dsl-plugin/blob/job-dsl-1.77/job-dsl-core/src/main/groovy/javaposse/jobdsl/dsl/jobs/WorkflowJob.groovy#L40">see</a>.
   * @param closure define one trigger per line, evaluated by {@link executeInContext}
   * @return the {@link BitBucketPPRTrigger} object with the list of triggers to be monitored
   */
  @DslExtensionMethod(context = TriggerContext.class)
  public Object bitbucketTriggers(Runnable closure) {
    BitBucketPPRHookJobDslContext context = new BitBucketPPRHookJobDslContext();
    executeInContext(closure, context);

    return new BitBucketPPRTrigger(context.triggers);
  }
}
