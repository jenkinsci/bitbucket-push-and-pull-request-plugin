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


package io.jenkins.plugins.bitbucketpushandpullrequest.cause;

import java.io.File;
import java.io.IOException;

import hudson.triggers.SCMTrigger;
import hudson.triggers.SCMTrigger.SCMTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPullRequestAction;


public class BitBucketPPRTriggerCause extends SCMTrigger.SCMTriggerCause {
  protected BitBucketPPRAction bitbucketAction;

  public BitBucketPPRTriggerCause(File pollingLog, BitBucketPPRAction bitbucketAction)
      throws IOException {
    super(pollingLog);
    this.bitbucketAction = bitbucketAction;
  }

  public BitBucketPPRAction getAction() {
    return this.bitbucketAction;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof SCMTriggerCause;
  }

  @Override
  public int hashCode() {
    return 3;
  }
}
