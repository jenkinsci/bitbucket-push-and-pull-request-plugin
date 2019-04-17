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
package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.*;

import javax.naming.OperationNotSupportedException;


public class BitBucketPPREvent {
  private String event;
  private String action;


  public BitBucketPPREvent(String eventAction) throws OperationNotSupportedException {
    String[] eventActionPair = eventAction.split(":");

    // diagnostics ping

    checkOperationSupportedException(eventActionPair);
    this.event = eventActionPair[0];
    this.action = eventActionPair[1];
  }

  private void checkOperationSupportedException(String[] eventActionPair)
      throws OperationNotSupportedException {

    boolean error = false;;

    if (REPOSITORY_EVENT.equalsIgnoreCase(eventActionPair[0])) {
      if (!(REPOSITORY_PUSH.equalsIgnoreCase(eventActionPair[1])
          || REPOSITORY_POST.equalsIgnoreCase(eventActionPair[1])
          || REPOSITORY_SERVER_PUSH.equalsIgnoreCase(eventActionPair[1]))) {
        error = true;
      }
    } else if (PULL_REQUEST_EVENT.equalsIgnoreCase(eventActionPair[0])) {
      if (!(PULL_REQUEST_APPROVED.equalsIgnoreCase(eventActionPair[1])
          || PULL_REQUEST_CREATED.equalsIgnoreCase(eventActionPair[1])
          || PULL_REQUEST_UPDATED.equalsIgnoreCase(eventActionPair[1]))) {
        error = true;
      }
    } else if (DIAGNOSTICS.equalsIgnoreCase(eventActionPair[0])) {
      if (!PING.equalsIgnoreCase(eventActionPair[1])) {
        error = true;
      }
    } else {
      error = true;
    }

    if (error) {
      throw new OperationNotSupportedException("The eventAction " + eventActionPair[0] + "  "
          + eventActionPair[1] + " is not supported.");
    }
  }

  public String getEvent() {
    return event;
  }

  public String getAction() {
    return action;
  }
}
