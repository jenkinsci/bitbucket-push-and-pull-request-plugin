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
/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc..
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRPullRequestAction extends BitBucketPPRAction {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRPullRequestAction.class.getName());

  public BitBucketPPRPullRequestAction(@Nonnull BitBucketPPRPayload payload) {
    super(payload);
  }

  public String getSourceBranch() {
    return payload.getPullRequest().getSource().getBranch().getName();
  }

  public String getTargetBranch() {
    return payload.getPullRequest().getDestination().getBranch().getName();
  }

  public String getPullRequestUrl() {
    return payload.getPullRequest().getLinks().getHtml().getHref();
  }

  @Override
  public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
    super.buildEnvVars(abstractBuild, envVars);

    envVars.put("BITBUCKET_BRANCH", getSourceBranch());
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_BRANCH: {0}", getSourceBranch());

    envVars.put("PULL_REQUEST_LINK", getPullRequestUrl());
    LOGGER.log(Level.FINEST, "Injecting PULL_REQUEST_LINK: {0}", getPullRequestUrl());
  }
}
