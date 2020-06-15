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
package io.jenkins.plugins.bitbucketpushandpullrequest.event;

import hudson.model.Run;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;


public class BitBucketPPREventContext {
  private SCM scmTrigger;
  private Run<?, ?> run;
  private BitBucketPPRAction action;
  private BitBucketPPRTriggerFilter filter;

  public BitBucketPPREventContext(BitBucketPPRAction action, SCM scmTrigger, Run<?, ?> run,
      BitBucketPPRTriggerFilter filter) {
    this.action = action;
    this.scmTrigger = scmTrigger;
    this.run = run;
    this.filter = filter;
  }

  public SCM getScmTrigger() {
    return scmTrigger;
  }

  public BitBucketPPRAction getAction() {
    return action;
  }

  public Run<?, ?> getRun() {
    return run;
  }

  public String getAbsoluteUrl() {
    return run.getAbsoluteUrl();
  }

  public BitBucketPPRTriggerFilter getFilter() {
    return this.filter;
  }

  @Override
  public String toString() {
    return "BitBucketPPREventContext [action=" + action + ", filter=" + filter + ", run=" + run
        + ", scmTrigger=" + scmTrigger + "]";
  }
}
