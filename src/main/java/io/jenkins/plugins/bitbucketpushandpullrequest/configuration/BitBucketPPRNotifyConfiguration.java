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
package io.jenkins.plugins.bitbucketpushandpullrequest.configuration;

import java.util.logging.Logger;

import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class BitBucketPPRNotifyConfiguration extends GlobalConfiguration {

	static final Logger logger = Logger.getLogger(BitBucketPPRNotifyConfiguration.class.getName());

	/** @return the singleton instance */
	public static BitBucketPPRNotifyConfiguration get() {
			return ExtensionList.lookupSingleton(BitBucketPPRNotifyConfiguration.class);
	}

	private boolean notifyBitBucket;

	public BitBucketPPRNotifyConfiguration() {
			// When Jenkins is restarted, load any saved configuration from disk.
			logger.info("Creating BitBucketPPRNotifyConfiguration...");
			this.notifyBitBucket = true;
			load();
	}

	/** @return the currently configured label, if any */
	public boolean isNotifyBitBucket() {
			return notifyBitBucket;
	}

	/**
	 * Together with {@link #isNotifyBitBucket}, binds to entry in {@code config.jelly}.
	 * @param notifyBitBucket the new value of this field
	 */
	@DataBoundSetter
	public void setNotifyBitBucket(boolean notifyBitBucket) {
			this.notifyBitBucket = notifyBitBucket;
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			req.bindJSON(this, formData);
			save();
			return true;
	}
}
