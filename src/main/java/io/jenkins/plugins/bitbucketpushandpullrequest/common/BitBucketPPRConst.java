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
package io.jenkins.plugins.bitbucketpushandpullrequest.common;

public final class BitBucketPPRConst {
  public static final String HOOK_URL = "bitbucket-hook";
  public static final String USER_AGENT = "Bitbucket-Webhooks/2.0";

  public static final String REPOSITORY_EVENT = "repo";
  public static final String REPOSITORY_CLOUD_PUSH = "push";
  public static final String REPOSITORY_POST = "post";

  public static final String DIAGNOSTICS = "diagnostics";
  public static final String PING = "ping";

  public static final String REPOSITORY_SERVER_PUSH = "refs_changed";

  public static final String PULL_REQUEST_CLOUD_EVENT = "pullrequest";
  public static final String PULL_REQUEST_CREATED = "created";
  public static final String PULL_REQUEST_APPROVED = "approved";
  public static final String PULL_REQUEST_UPDATED = "updated";
  public static final String PULL_REQUEST_MERGED = "fulfilled";
  public static final String PULL_REQUEST_DECLINED = "rejected";
  public static final String PULL_REQUEST_COMMENT_CREATED = "comment_created";
  public static final String PULL_REQUEST_COMMENT_UPDATED = "comment_updated";
  public static final String PULL_REQUEST_COMMENT_DELETED = "comment_deleted";

  public static final String PULL_REQUEST_SERVER_EVENT = "pr";
  public static final String PULL_REQUEST_SERVER_CREATED = "opened";
  public static final String PULL_REQUEST_SERVER_APPROVED = "approved";
  public static final String PULL_REQUEST_SERVER_UPDATED = "modified";
  public static final String PULL_REQUEST_SERVER_SOURCE_UPDATED = "from_ref_updated";
  public static final String PULL_REQUEST_SERVER_MERGED = "merged";
  public static final String PULL_REQUEST_SERVER_DECLINED = "declined";
  public static final String PULL_REQUEST_SERVER_COMMENT_CREATED = "comment:added";

  public static final String PULL_REQUEST_REVIEWER = "REVIEWER";
  public static final String PULL_REQUEST_PARTICIPANT = "PARTICIPANT";
  public static final String DEPRECATED_X_HEADER_REPO_POST = "repo:post";
  public static final String X_EVENT_KEY = "x-event-key";
  public static final String PAYLOAD_PFX = "payload=";
  public static final String APPLICATION_X_WWW_FORM_URLENCODED =
      "application/x-www-form-urlencoded";

  private BitBucketPPRConst() {
    throw new AssertionError();
  }
}
