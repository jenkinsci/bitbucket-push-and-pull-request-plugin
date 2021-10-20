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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import hudson.Util;
import hudson.model.Job;
import hudson.scm.PollingResult;
import hudson.util.StreamTaskListener;
import jenkins.triggers.SCMTriggerItem;

public class BitBucketPPRPollingRunnable implements Runnable {
  private Job<?, ?> job;
  private File logFile;
  private BitBucketPPRPollResultListener bitbucketPollResultListener;

  private static final Logger logger =
      Logger.getLogger(BitBucketPPRPollingRunnable.class.getName());


  public BitBucketPPRPollingRunnable(@Nonnull Job<?, ?> job, @Nonnull File logFile,
      @Nonnull BitBucketPPRPollResultListener bitbucketPollResultListener) {
    this.job = job;
    this.bitbucketPollResultListener = bitbucketPollResultListener;
    this.logFile = logFile;
  }

  public void run() {
    logger.log(Level.FINE, "Run method called.");

    try (StreamTaskListener streamListener = new StreamTaskListener(logFile)) {
      PrintStream logger = streamListener.getLogger();
      long start = System.currentTimeMillis();
      logger.println("Started on " + DateFormat.getDateTimeInstance().format(new Date()));

      PollingResult pollingResult =
          SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job).poll(streamListener);
      logger.println("Done. Took " + Util.getTimeSpanString(System.currentTimeMillis() - start));
      bitbucketPollResultListener.onPollSuccess(pollingResult);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed {0}", e.getMessage());
    } catch (NullPointerException e) {
      logger.log(Level.SEVERE, "NullPointerException: Failed to record SCM polling {0}",
          e.getMessage());
    } catch (RuntimeException e) {
      logger.log(Level.SEVERE, "RuntimeException: Failed to record SCM polling {0}",
          e.getMessage());
      bitbucketPollResultListener.onPollError(e);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed ", e);
    }
  }
  
}
