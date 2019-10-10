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
package io.jenkins.plugins.bitbucketpushandpullrequest.model;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.*;
import javax.naming.OperationNotSupportedException;


public class BitBucketPPREvent {
  private String event;
  private String action;


  public BitBucketPPREvent(String eventAction) throws OperationNotSupportedException {
    String[] eventActionPair = eventAction.split(":");

    event = eventActionPair[0];

    if (eventActionPair.length == 3 && eventActionPair[1].equalsIgnoreCase("reviewer")) {
      action = eventActionPair[2];
    } else {
      action = eventActionPair[1];
    }

    checkOperationSupportedException(event, action);

  }

  private void checkOperationSupportedException(String event, String action)
      throws OperationNotSupportedException {

    boolean error = false;

    if (REPOSITORY_EVENT.equalsIgnoreCase(event)) {
      if (!(REPOSITORY_PUSH.equalsIgnoreCase(action) || REPOSITORY_POST.equalsIgnoreCase(action)
          || REPOSITORY_SERVER_PUSH.equalsIgnoreCase(action))) {
        error = true;
      }
    } else if (PULL_REQUEST_EVENT.equalsIgnoreCase(event)
        || PULL_REQUEST_SERVER_EVENT.equalsIgnoreCase(event)) {
      if (!(PULL_REQUEST_APPROVED.equalsIgnoreCase(action)
          || PULL_REQUEST_CREATED.equalsIgnoreCase(action)
          || PULL_REQUEST_UPDATED.equalsIgnoreCase(action)
          || PULL_REQUEST_MERGED.equalsIgnoreCase(action)
          || PULL_REQUEST_SERVER_CREATED.equalsIgnoreCase(action)
          || PULL_REQUEST_SERVER_UPDATED.equalsIgnoreCase(action)
          || PULL_REQUEST_SERVER_APPROVED.equalsIgnoreCase(action)
          || PULL_REQUEST_SERVER_MERGED.equalsIgnoreCase(action))) {
        error = true;
      }
    } else if (DIAGNOSTICS.equalsIgnoreCase(event)) {
      if (!PING.equalsIgnoreCase(action)) {
        error = true;
      }
    } else {
      error = true;
    }

    if (error) {
      throw new OperationNotSupportedException(
          "The eventAction " + event + "  " + action + " is not supported.");
    }
  }

  public String getEvent() {
    return event;
  }

  public String getAction() {
    return action;
  }

  @Override
  public String toString() {
    return "BitBucketPPREvent [event=" + event + ", action=" + action + "]";
  }
}
