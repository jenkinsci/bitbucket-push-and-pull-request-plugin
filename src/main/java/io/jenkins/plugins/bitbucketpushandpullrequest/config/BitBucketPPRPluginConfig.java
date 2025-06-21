package io.jenkins.plugins.bitbucketpushandpullrequest.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class BitBucketPPRPluginConfig extends GlobalConfiguration {
  private static final Logger logger = Logger.getLogger(BitBucketPPRPluginConfig.class.getName());
  public static final String BITBUCKET_PPR_PLUGIN_CONFIGURATION_ID =
      "bitbucket-ppr-plugin-configuration";

  public String hookUrl;

  public boolean notifyBitBucket;

  public boolean useJobNameAsBuildKey;

  public String credentialsId;

  public String singleJob;

  public String propagationUrl;

  public BitBucketPPRPluginConfig() {
    logger.fine("Read bitbucket push and pull request plugin global configuration.");
    this.notifyBitBucket = true;
    load();
  }

  public static BitBucketPPRPluginConfig getInstance() {
    return ExtensionList.lookupSingleton(BitBucketPPRPluginConfig.class);
  }

  @DataBoundSetter
  public void setHookUrl(String hookUrl) {
    if (hookUrl == null || isEmpty(hookUrl)) {
      this.hookUrl = "";
    } else {
      this.hookUrl = hookUrl;
    }
    save();
  }

  @DataBoundSetter
  public void setPropagationUrl(String propagationUrl) {

    this.propagationUrl = propagationUrl;

    save();
  }

  public FormValidation doCheckPropagationUrl(@QueryParameter String value) {
    if (value == null || value.isEmpty()) {
      return FormValidation.ok();
    }

    try {
      new URL(value); // This will throw MalformedURLException if the URL is not valid
      return FormValidation.ok();
    } catch (MalformedURLException e) {
      return FormValidation.error("This is not a valid URL. Please enter a correct URL.");
    }
  }

  public boolean isHookUrlSet() {
    return !isEmpty(hookUrl);
  }

  public String getHookUrl() {
    return hookUrl;
  }

  public boolean getNotifyBitBucket() {
    return notifyBitBucket;
  }

  public String getPropagationUrl() {
    return propagationUrl;
  }

  public boolean isPropagationUrlSet() {
    return !isEmpty(propagationUrl);
  }

  @DataBoundSetter
  public void setNotifyBitBucket(boolean notifyBitBucket) {
    this.notifyBitBucket = notifyBitBucket;
  }

  public boolean getUseJobNameAsBuildKey() {
    return useJobNameAsBuildKey;
  }

  @DataBoundSetter
  public void setUseJobNameAsBuildKey(boolean useJobNameAsBuildKey) {
    this.useJobNameAsBuildKey = useJobNameAsBuildKey;
    save();
  }

  @DataBoundSetter
  public void setCredentialsId(@CheckForNull String credentialsId) {
    this.credentialsId = credentialsId;
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  @DataBoundSetter
  public void setSingleJob(String singleJob) {
    if (isEmpty(singleJob)) {
      this.singleJob = "";
    } else {
      this.singleJob = singleJob.trim();
    }
    save();
  }

  public boolean isSingleJobSet() {
    return !isEmpty(singleJob);
  }

  public String getSingleJob() {
    return singleJob;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return "Bitbucket Push and Pull Request";
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject formData) {
    req.bindJSON(this, formData);
    save();
    return true;
  }

  public ListBoxModel doFillCredentialsIdItems(
      @AncestorInPath Item context,
      @QueryParameter String remote,
      @QueryParameter String credentialsId) {

    if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
        || context != null && !context.hasPermission(Item.EXTENDED_READ)) {
      return new StandardListBoxModel().includeCurrentValue(credentialsId);
    }

    return new StandardListBoxModel()
        .includeEmptyValue()
        .includeMatchingAs(
            ACL.SYSTEM2,
            Jenkins.get(),
            StandardCredentials.class,
            Collections.emptyList(),
            CredentialsMatchers.always())
        .includeCurrentValue(credentialsId);
  }
}
