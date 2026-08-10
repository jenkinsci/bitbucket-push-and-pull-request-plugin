/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2026, Christian Del Monte.
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

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerNotificationConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;


public class BitBucketPPREventContext {
  private SCM scmTrigger;
  private Run<?, ?> run;
  private BitBucketPPRAction action;
  private BitBucketPPRTriggerFilter filter;
  private UserRemoteConfig userRemoteConfig;
  private BitBucketPPRTrigger trigger;
  private BitBucketPPRServerNotificationConfig notificationConfig;

  public BitBucketPPREventContext(BitBucketPPRTrigger trigger, BitBucketPPRAction action,
      SCM scmTrigger, Run<?, ?> run, BitBucketPPRTriggerFilter filter) {
    this.action = action;
    this.scmTrigger = scmTrigger;
    this.run = run;
    this.filter = filter;
    this.userRemoteConfig = getUserRemoteConfigs(scmTrigger);
    
    this.trigger = trigger;
  }

  // The build URL is sent to Bitbucket in the build-status payload. It is not HTML rendering.
  @SuppressWarnings("deprecation")
  public StandardCredentials getStandardCredentials(String url) throws Exception {
    
    final StandardCredentials credentials =
        CredentialsProvider.findCredentialById(getCredentialsId(), StandardCredentials.class, run,
            URIRequirementBuilder.fromUri(url).build());
    
            if (credentials != null) {
      return credentials;
    }

    throw new Exception("No Credentials found for run: " + run.getNumber() + " - url: " + url
        + " - credentialsId: " + getCredentialsId() + " - absolute url : " + run.getAbsoluteUrl());
  }

  // The build URL is sent to Bitbucket in the build-status payload. It is not HTML rendering.
  @SuppressWarnings("deprecation")
  public StandardCredentials getStandardCredentials(String credentialsId, String url) throws Exception {
    
    final StandardCredentials credentials =
        CredentialsProvider.findCredentialById(credentialsId, StandardCredentials.class, run,
            URIRequirementBuilder.fromUri(url).build());
    
            if (credentials != null) {
      return credentials;
    }

    throw new Exception("No Credentials found for run: " + run.getNumber() + " - url: " + url
        + " - credentialsId: " + getCredentialsId() + " - absolute url : " + run.getAbsoluteUrl());
  }

  /**
   * @deprecated The Server notification path resolves the credential from the notification config
   *     pair via {@link #getStandardCredentials(String, String)}; kept for binary compatibility.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public StandardCredentials getStandardCredentials() throws Exception {
    return getStandardCredentials(getCredentialsId(), getUrl());
  }

  /**
   * @deprecated Not used by the notification path; kept for binary compatibility.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public StringCredentials getSecretTextCredentials() throws Exception {
    final StringCredentials credentials = CredentialsProvider.findCredentialById(getCredentialsId(),
        StringCredentials.class, run, URIRequirementBuilder.fromUri(getUrl()).build());
    if (credentials != null) {
      return credentials;
    }
    throw new Exception("No Credentials found for run: " + run.getNumber() + " - credentialsId: "
        + getCredentialsId());
  }

  /**
   * @deprecated SCM URL of the first configured remote; kept for binary compatibility.
   */
  @Deprecated
  public String getUrl() {
    return userRemoteConfig.getUrl();
  }

  protected BitBucketPPRPluginConfig getGlobalConfig() {
    return BitBucketPPRPluginConfig.getInstance();
  }

  public String getCredentialsId() {
    if (StringUtils.isNotBlank(trigger.getCredentialsId()))
      return trigger.getCredentialsId();
    if (StringUtils.isNotBlank(getGlobalConfig().getCredentialsId()))
      return getGlobalConfig().getCredentialsId();
    throw new RuntimeException("Credentials for propagation are not set.");
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

  // The build URL is sent to Bitbucket in the build-status payload. It is not HTML rendering.
  @SuppressWarnings("deprecation")
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

  public BitBucketPPRServerNotificationConfig getNotificationConfig() {
    // Resolved once per event so that the observer (endpoint) and the client (credential) see the
    // same pair: a concurrent config change cannot split the endpoint from the credential.
    if (notificationConfig == null) {
      notificationConfig = BitBucketPPRServerNotificationConfig.resolve(trigger, getGlobalConfig());
    }
    return notificationConfig;
  }

  @Override
  public String toString() {
    return "BitBucketPPREventContext [scmTrigger=" + scmTrigger + ", run=" + run + ", action="
        + action + ", filter=" + filter + ", userRemoteConfig=" + userRemoteConfig + ", url=" + trigger.propagationUrl
        + ", trigger=" + trigger + "]";
  }
}
