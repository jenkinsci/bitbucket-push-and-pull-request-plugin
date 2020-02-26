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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;

public class BitBucketPPRPullRequestAction extends InvisibleAction implements BitBucketPPRAction {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRPullRequestAction.class.getName());

  private final @Nonnull BitBucketPPRPayload payload;
  private List<String> scmUrls = new ArrayList<>(2);

  public BitBucketPPRPullRequestAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;

    scmUrls.add(payload.getRepository().getLinks().getHtml().getHref());
  }

  @Override
  public String getSourceBranch() {
    return payload.getPullRequest().getSource().getBranch().getName();
  }

  @Override
  public String getTargetBranch() {
    return payload.getPullRequest().getDestination().getBranch().getName();
  }

  @Override
  public String getPullRequestUrl() {
    return payload.getPullRequest().getLinks().getHtml().getHref();
  }

  @Override
  public String getTitle() {
    return payload.getPullRequest().getTitle();
  }

  @Override
  public String getDescription() {
    return payload.getPullRequest().getDescription();
  }

  @Override
  public BitBucketPPRPayload getPayload() {
    return payload;
  }

  @Override
  public String getScm() {
    return payload.getRepository().getScm() != null
    ? payload.getRepository().getScm()
    : "git";
  }

  @Override
  public String getUser() {
    return payload.getActor().getNickname();
  }

  @Override
  public String getType() {
    return null;
  }

  @Override
  public String getRepositoryName() {
    return payload.getRepository().getName();
  }

  @Override
  public List<String> getScmUrls() {
    return scmUrls;
  }

  @Override
  public String getPullRequestId() {
    return payload.getPullRequest().getId();
  }

  @Override
  public String getRepositoryUuid() {
    return null;
  }

  public void buildEnvironment(@Nonnull Run<?, ?> run, @Nonnull EnvVars env) {
    env.put("BITBUCKET_PAYLOAD", payload.toString());
  }
}
