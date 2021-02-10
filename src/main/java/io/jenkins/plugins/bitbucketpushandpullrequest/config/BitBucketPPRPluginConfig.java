package io.jenkins.plugins.bitbucketpushandpullrequest.config;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

@Extension
public class BitBucketPPRPluginConfig extends GlobalConfiguration {
  private static final Logger logger = Logger.getLogger(BitBucketPPRPluginConfig.class.getName());
  public static final String BITBUCKET_PPR_PLUGIN_CONFIGURATION_ID = "bitbucket-ppr-plugin-configuration";

  @SuppressWarnings("unused")
  public String hookUrl;

  public BitBucketPPRPluginConfig() {
    logger.fine("Read bitbucket push and pull request plugin global configuration.");
    load();
  }

  public static BitBucketPPRPluginConfig getInstance() {
    return ExtensionList.lookupSingleton(BitBucketPPRPluginConfig.class);
  }

  @DataBoundSetter
  public void setHookUrl(String hookUrl) {
    if (isEmpty(hookUrl)) {
      this.hookUrl = "";
    } else {
      this.hookUrl = hookUrl;
    }
    save();
  }

  public boolean isHookUrlSet() {
    return ! isEmpty(hookUrl);
  }

  public String getHookUrl() {
    load();
    return hookUrl;
  }

  @Override
  public String getDisplayName() {
    return "Bitbucket Push and Pull Request";
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
    req.bindJSON(this, formData);
    save();
    return true;
  }
}
