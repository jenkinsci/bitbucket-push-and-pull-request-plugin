/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package io.jenkins.plugins.bitbucketpushandpullrequest.util;

public final class BitBucketPPRConsts {
  public static final String HOOK_URL = "bitbucket-hook";
  public static final String USER_AGENT = "Bitbucket-Webhooks/2.0";

  public static final String REPOSITORY_EVENT = "repo";
  public static final String REPOSITORY_PUSH = "push";
  public static final String REPOSITORY_POST = "post";
  
  public static final String REPOSITORY_SERVER_PUSH = "refs_changed";

  public static final String PULL_REQUEST_EVENT = "pullrequest";
  public static final String PULL_REQUEST_CREATED = "created";
  public static final String PULL_REQUEST_APPROVED = "approved";
  public static final String PULL_REQUEST_UPDATED = "updated";

  public static final String PULL_REQUEST_REVIEWER = "REVIEWER";
  public static final String PULL_REQUEST_PARTICIPANT = "PARTICIPANT";

  private BitBucketPPRConsts() {
    throw new AssertionError();
  }
}
