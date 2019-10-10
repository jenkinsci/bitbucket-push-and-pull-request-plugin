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

package io.jenkins.plugins.bitbucketpushandpullrequest;

import hudson.Util;
import hudson.model.Job;
import hudson.scm.PollingResult;
import hudson.util.StreamTaskListener;
import jenkins.triggers.SCMTriggerItem;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitBucketPPRPollingRunnable implements Runnable {
  Job<?, ?> job;
  File logFile;

  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPRPollingRunnable.class.getName());

  BitBucketPPRPollResultListener bitbucketPollResultListener;

  public BitBucketPPRPollingRunnable(Job<?, ?> job, File logFile,
      BitBucketPPRPollResultListener bitbucketPollResultListener) {
    this.job = job;
    this.bitbucketPollResultListener = bitbucketPollResultListener;
    this.logFile = logFile;
  }

  public void run() {
    LOGGER.log(Level.INFO, "Run method called.");

    try (StreamTaskListener streamListener = new StreamTaskListener(logFile)) {
      exec(streamListener);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed {0}", e.getMessage());
    }
  }

  private void exec(StreamTaskListener streamListener) {
    try {
      PrintStream logger = streamListener.getLogger();
      long start = System.currentTimeMillis();
      logger.println("Started on " + DateFormat.getDateTimeInstance().format(new Date()));

      PollingResult pollingResult =
          SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job).poll(streamListener);
      logger.println("Done. Took " + Util.getTimeSpanString(System.currentTimeMillis() - start));
      bitbucketPollResultListener.onPollSuccess(pollingResult);

    } catch (NullPointerException e) {
      LOGGER.log(Level.SEVERE, "NullPointerException: Failed to record SCM polling {0}",
          e.getMessage());
    } catch (RuntimeException e) {
      e.printStackTrace(streamListener.error("Failed to record SCM polling"));
      LOGGER.log(Level.SEVERE, "RuntimeException: Failed to record SCM polling {0}",
          e.getMessage());
      bitbucketPollResultListener.onPollError(e);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Failed ", e);
    }
  }
}
