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
/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc..
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
 */

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.google.gson.Gson;

import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRAction extends InvisibleAction {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRAction.class.getName());

  protected final @Nonnull BitBucketPPRPayload payload;
  protected String scm;
  protected String user;
  protected List<String> scmUrls = new ArrayList<>(2);
  protected String branchName;
  protected String type;
  protected String repositoryName;
  protected String pullRequestId;
  protected String repositoryUuid;

  public BitBucketPPRAction(@Nonnull BitBucketPPRPayload payload) {
    this.payload = payload;

    if (payload.getRepository() != null) {
      user = payload.getActor().getNickname();
      repositoryName = payload.getRepository().getName();
      scm = payload.getRepository().getScm() != null ? payload.getRepository().getScm() : "git";
      scmUrls.add(payload.getRepository().getLinks().getHtml().getHref());
    }
  }

  @Nonnull
  public BitBucketPPRPayload getPayload() {
    return payload;
  }

  public String getScm() {
    return scm;
  }

  public String getUser() {
    return user;
  }

  public String getBranchName() {
    return branchName;
  }

  public String getType() {
    return type;
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  public List<String> getScmUrls() {
    return scmUrls;
  }
  
  public String getPullRequestId() {
    return pullRequestId;
  }

  public String getRepositoryUuid() {
    return repositoryUuid;
  }
}
