package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import hudson.model.InvisibleAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class BitBucketPPRActionAbstract extends InvisibleAction {
  private String propagationUrl = "";

  public BitBucketPPRActionAbstract() {
    if (!isEmpty(getGlobalConfig().propagationUrl)) {
      this.setPropagationUrl(getGlobalConfig().propagationUrl);
    }
  }

  private static final BitBucketPPRPluginConfig globalConfig =
      BitBucketPPRPluginConfig.getInstance();

  public BitBucketPPRPluginConfig getGlobalConfig() {
    return globalConfig;
  }

  public void setPropagationUrl(String propagationUrl) {
    this.propagationUrl = propagationUrl;
  }

  public String getPropagationUrl() {
    return propagationUrl;
  }
}
