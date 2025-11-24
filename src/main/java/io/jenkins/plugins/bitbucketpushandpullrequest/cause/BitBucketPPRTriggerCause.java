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


package io.jenkins.plugins.bitbucketpushandpullrequest.cause;

import java.io.File;
import java.io.IOException;

import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;


public class BitBucketPPRTriggerCause extends SCMTrigger.SCMTriggerCause {
  protected BitBucketPPRAction bitbucketAction;
  protected BitBucketPPRHookEvent bitBucketHookEvent;

  public BitBucketPPRTriggerCause(File pollingLog, BitBucketPPRAction bitbucketAction,
      BitBucketPPRHookEvent bitBucketHookEvent)
      throws IOException {
    super(pollingLog);
    this.bitbucketAction = bitbucketAction;
    this.bitBucketHookEvent = bitBucketHookEvent;
  }

  public String getHookEvent() {
    return this.bitBucketHookEvent.getBitbucketEventKey();
  }

  public BitBucketPPRAction getAction() {
    return this.bitbucketAction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((bitbucketAction == null) ? 0 : bitbucketAction.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    BitBucketPPRTriggerCause other = (BitBucketPPRTriggerCause) obj;
    if (bitbucketAction == null) {
        return other.bitbucketAction == null;
    } else return bitbucketAction.equals(other.bitbucketAction);
  }

  @Override
  public String toString() {
    return "BitBucketPPRTriggerCause [bitbucketAction=" + bitbucketAction + "]";
  }
}
