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
package io.jenkins.plugins.bitbucketpushandpullrequest.model.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRActor;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRApproval;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRPullRequest;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRPush;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRRepository;


public class BitBucketPPROldPost implements BitBucketPPRPayload, Serializable {
  private static final long serialVersionUID = -3802484750667918594L;
  private @SerializedName("canon_url") String canonUrl;
  private List<BitBucketPPROldCommit> commits = new ArrayList<>();
  private BitBucketPPROldRepository repository;
  private String user;

  public String getCanonUrl() {
    return canonUrl;
  }

  public void setCanonUrl(final String canonUrl) {
    this.canonUrl = canonUrl;
  }

  public List<BitBucketPPROldCommit> getCommits() {
    return commits;
  }

  public void setCommits(final List<BitBucketPPROldCommit> commits) {
    this.commits = commits;
  }

  public BitBucketPPROldRepository getOldRepository() {
    return repository;
  }

  public void setOldRepository(final BitBucketPPROldRepository repository) {
    this.repository = repository;
  }

  public String getUser() {
    return user;
  }

  public void setUser(final String user) {
    this.user = user;
  }

  @Override
  public BitBucketPPRRepository getRepository() {
    return null;
  }

  @Override
  public BitBucketPPRPullRequest getPullRequest() {
    return null;
  }

  @Override
  public BitBucketPPRPush getPush() {
    return null;
  }

  @Override
  public BitBucketPPRActor getActor() {
    return null;
  }

  @Override
  public BitBucketPPRApproval getApproval() {
    return null;
  }

  @Override
  public String toString() {
    return new com.google.gson.Gson().toJson(this);
  }
}
