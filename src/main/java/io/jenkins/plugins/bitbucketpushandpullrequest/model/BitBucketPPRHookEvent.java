/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2021, CloudBees, Inc.
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

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.DIAGNOSTICS;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PING;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_APPROVED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_CLOUD_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_COMMENT_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_COMMENT_DELETED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_COMMENT_UPDATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_DECLINED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_MERGED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_APPROVED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_COMMENT_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_CREATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_DECLINED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_MERGED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_SOURCE_UPDATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_UPDATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_UPDATED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_CLOUD_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_POST;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_SERVER_PUSH;
import javax.naming.OperationNotSupportedException;

/**
 * Extracts event and action from the event key sent by Bitbucket and verifies that they are
 * supported by the plugin.
 * 
 * @author cdelmonte
 * 
 */
public class BitBucketPPRHookEvent {
  private String event;
  private String action;
  private String bitbucketEventKey;

  public BitBucketPPRHookEvent(String bitbucketEventKey) throws OperationNotSupportedException {
    this.bitbucketEventKey = bitbucketEventKey;
    setEventAndAction();
    checkOperationSupported();
  }

  private void setEventAndAction() {
    String[] eventActionPair = bitbucketEventKey.split(":");
    event = eventActionPair[0];

    if (eventActionPair.length == 3 && eventActionPair[1].equalsIgnoreCase("reviewer")) {
      action = eventActionPair[2];
    } else if (eventActionPair.length == 3 && eventActionPair[1].equalsIgnoreCase("comment")) {
      action = eventActionPair[1] + ":" + eventActionPair[2];
    } else {
      action = eventActionPair[1];
    }
  }

  private void checkOperationSupported() throws OperationNotSupportedException {


    if (REPOSITORY_EVENT.equalsIgnoreCase(event) && (REPOSITORY_CLOUD_PUSH.equalsIgnoreCase(action)
        || REPOSITORY_POST.equalsIgnoreCase(action)
        || REPOSITORY_SERVER_PUSH.equalsIgnoreCase(action))) {
      return;
    }

    if (PULL_REQUEST_CLOUD_EVENT.equalsIgnoreCase(event)
        || PULL_REQUEST_SERVER_EVENT.equalsIgnoreCase(event)
            && (PULL_REQUEST_APPROVED.equalsIgnoreCase(action)
                || PULL_REQUEST_CREATED.equalsIgnoreCase(action)
                || PULL_REQUEST_UPDATED.equalsIgnoreCase(action)
                || PULL_REQUEST_MERGED.equalsIgnoreCase(action)
                || PULL_REQUEST_COMMENT_CREATED.equalsIgnoreCase(action)
                || PULL_REQUEST_COMMENT_UPDATED.equalsIgnoreCase(action)
                || PULL_REQUEST_DECLINED.equalsIgnoreCase(action)
                || PULL_REQUEST_COMMENT_DELETED.equalsIgnoreCase(action)
                || PULL_REQUEST_SERVER_CREATED.equalsIgnoreCase(action)
                || PULL_REQUEST_SERVER_UPDATED.equalsIgnoreCase(action)
                || PULL_REQUEST_SERVER_SOURCE_UPDATED.equalsIgnoreCase(action)
                || PULL_REQUEST_SERVER_APPROVED.equalsIgnoreCase(action)
                || PULL_REQUEST_SERVER_MERGED.equalsIgnoreCase(action)
                || PULL_REQUEST_SERVER_DECLINED.equalsIgnoreCase(action)
                || PULL_REQUEST_SERVER_COMMENT_CREATED.equalsIgnoreCase(action))) {
      return;
    }

    if (DIAGNOSTICS.equalsIgnoreCase(event) && PING.equalsIgnoreCase(action)) {
      return;
    }

    throw new OperationNotSupportedException(
        String.format("The event action %s : %s with bitbucket Event Key %s is not supported.",
            event, action, bitbucketEventKey));
  }

  public String getEvent() {
    return event;
  }

  public String getBitbucketEventKey() {
    return bitbucketEventKey;
  }

  public String getAction() {
    return action;
  }

  @Override
  public String toString() {
    return String.format("BitBucketPPREvent [bitbucketEventKey = %s, event = %s, action = %s]",
        bitbucketEventKey, event, action);
  }
}
