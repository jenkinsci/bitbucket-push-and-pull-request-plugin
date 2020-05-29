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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import java.util.List;

import hudson.model.Action;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;

public interface BitBucketPPRAction extends Action {

  public BitBucketPPRPayload getPayload();

  public String getScm();

  public String getLinkHtml();

  public String getLinkSelf();

  public String getLinkApprove();

  public String getLinkDecline();

  public String getLinkStatuses();

  public String getUser();

  public String getSourceBranch();

  public String getTargetBranch();

  public String getType();

  public String getRepositoryName();

  // TODO: do we really neeed it?
  public List<String> getScmUrls();

  public String getPullRequestId();

  public String getRepositoryId();

  public String getPullRequestUrl();

  public String getTitle();

  public String getDescription();

  public String getComment();
}
