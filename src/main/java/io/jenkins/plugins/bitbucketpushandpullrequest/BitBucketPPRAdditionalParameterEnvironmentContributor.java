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

package io.jenkins.plugins.bitbucketpushandpullrequest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.BitBucketPPRPullRequestCause;

@Extension
public class BitBucketPPRAdditionalParameterEnvironmentContributor extends EnvironmentContributor {
  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPRAdditionalParameterEnvironmentContributor.class.getName());

  @Override
  public void buildEnvironmentFor(@Nonnull Run run, EnvVars envVars, TaskListener taskListener)
      throws IOException, InterruptedException {

    BitBucketPPRPullRequestCause cause = (BitBucketPPRPullRequestCause) run.getCause(BitBucketPPRPullRequestCause.class);
    if (cause == null) {
      return;
    }

    String sourceBranch = cause.getPullRequestPayLoad().getSourceBranch();
    putEnvVar(envVars, "BITBUCKET_SOURCE_BRANCH", sourceBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", sourceBranch);

    String targetBranch = cause.getPullRequestPayLoad().getTargetBranch();
    putEnvVar(envVars, "BITBUCKET_TARGET_BRANCH", targetBranch);
    LOGGER.log(Level.FINEST, "Injecting BITBUCKET_TARGET_BRANCH: {0}", targetBranch);
  }

  private static void putEnvVar(EnvVars envs, String name, String value) {
    envs.put(name, getString(value, ""));
  }

  private static String getString(String actual, String d) {
    return actual == null ? d : actual;
  }
}
