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


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilterDescriptor;
import jenkins.model.Jenkins;


public class BitBucketPPRRepositoryTriggerFilter extends BitBucketPPRTriggerFilter {
  public BitBucketPPRRepositoryActionFilter actionFilter;

  @DataBoundConstructor
  public BitBucketPPRRepositoryTriggerFilter(BitBucketPPRRepositoryActionFilter actionFilter) {
    this.actionFilter = actionFilter;
  }

  @Override
  public boolean shouldScheduleJob(BitBucketPPRAction bitbucketAction) {
    return actionFilter.shouldTriggerBuild(bitbucketAction);
  }

  @Override
  public BitBucketPPRTriggerCause getCause(File pollingLog, BitBucketPPRAction action)
      throws IOException {
    return actionFilter.getCause(pollingLog, action);
  }

  @Override
  public BitBucketPPRRepositoryActionFilter getActionFilter() {
    return actionFilter;
  }

  @Extension
  public static class FilterDescriptorImpl extends BitBucketPPRTriggerFilterDescriptor {
    public String getDisplayName() {
      return "Push";
    }

    public List<BitBucketPPRRepositoryActionDescriptor> getActionDescriptors() {
      // you may want to filter this list of descriptors here, if you are being very fancy
      return Jenkins.getInstance().getDescriptorList(BitBucketPPRRepositoryActionFilter.class);
    }
  }

}
