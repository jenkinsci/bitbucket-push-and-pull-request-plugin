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
package io.jenkins.plugins.bitbucketpushandpullrequest.event;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;

public class BitBucketPPREventContext {
  private SCM scmTrigger;
  private Run<?, ?> run;
  private BitBucketPPRAction action;
  private BitBucketPPRTriggerFilter filter;
  private UserRemoteConfig userRemoteConfig;
  private String url;
  private BitBucketPPRTrigger trigger;

  public BitBucketPPREventContext(BitBucketPPRTrigger trigger, BitBucketPPRAction action,
      SCM scmTrigger, Run<?, ?> run, BitBucketPPRTriggerFilter filter) throws Exception {
    this.action = action;
    this.scmTrigger = scmTrigger;
    this.run = run;
    this.filter = filter;
    this.userRemoteConfig = getUserRemoteConfigs(scmTrigger);
    this.trigger = trigger;
    this.url = userRemoteConfig.getUrl();
  }

  public StandardCredentials getStandardCredentials() throws Exception {
    final StandardCredentials credentials =
        CredentialsProvider.findCredentialById(getCredentialsId(), StandardCredentials.class, run,
            URIRequirementBuilder.fromUri(url).build());
    if (credentials != null) {
      return credentials;
    }
    throw new Exception(String.format(
        "No Credentials found for run: %s - url: %s - credentialsId: %s - absolute url : %s",
        run.getNumber(), url, getCredentialsId(), run.getAbsoluteUrl()));
  }

  protected BitBucketPPRPluginConfig getGlobalConfig() {
    return BitBucketPPRPluginConfig.getInstance();
  }

  public String getUrl() {
    return url;
  }

  public String getCredentialsId() {
    if (trigger.credentialsId != null)
      return trigger.credentialsId;
    if (getGlobalConfig().credentialsId != null)
      return getGlobalConfig().credentialsId;
    if (userRemoteConfig.getCredentialsId() != null)
      return userRemoteConfig.getCredentialsId();
    return "";
  }

  public UserRemoteConfig getUserRemoteConfig() {
    return userRemoteConfig;
  }

  public SCM getScmTrigger() {
    return scmTrigger;
  }

  public BitBucketPPRAction getAction() {
    return action;
  }

  public Run<?, ?> getRun() {
    return run;
  }

  public String getAbsoluteUrl() {
    return run.getAbsoluteUrl();
  }

  public int getBuildNumber() {
    return run.getNumber();
  }

  public BitBucketPPRTriggerFilter getFilter() {
    return this.filter;
  }

  public UserRemoteConfig getUserRemoteConfigs(SCM scm) {
    GitSCM gitSCM = (GitSCM) scm;
    UserRemoteConfig config = gitSCM.getUserRemoteConfigs().get(0);
    return config;
  }

  @Override
  public String toString() {
    return "BitBucketPPREventContext [scmTrigger=" + scmTrigger + ", run=" + run + ", action="
        + action + ", filter=" + filter + ", userRemoteConfig=" + userRemoteConfig + ", url=" + url
        + ", trigger=" + trigger + "]";
  }
}
