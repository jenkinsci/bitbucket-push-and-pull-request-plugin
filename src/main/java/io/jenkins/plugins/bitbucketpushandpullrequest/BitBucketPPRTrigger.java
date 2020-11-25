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

package io.jenkins.plugins.bitbucketpushandpullrequest;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.jelly.XMLOutput;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.queue.QueueTaskFuture;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.SequentialExecutionQueue;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventType;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.JobNotStartedException;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRFilterMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilterDescriptor;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryPushActionFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository.BitBucketPPRRepositoryTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRProject;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRUtils;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;

public class BitBucketPPRTrigger extends Trigger<Job<?, ?>> {
  private static final Logger logger = Logger.getLogger(BitBucketPPRTrigger.class.getName());
  private List<BitBucketPPRTriggerFilter> triggers;

  {
    System.setErr(BitBucketPPRUtils.createLoggingProxyForErrors(System.err));
  }

  @DataBoundConstructor
  public BitBucketPPRTrigger(List<BitBucketPPRTriggerFilter> triggers) {
    this.triggers = triggers;
  }

  @Override
  public Object readResolve() throws ObjectStreamException {
    super.readResolve();

    // TODO: is this he place for that? And why only for push?
    if (triggers == null) {
      BitBucketPPRRepositoryPushActionFilter repositoryPushActionFilter = new BitBucketPPRRepositoryPushActionFilter(
          false, false, spec);
      BitBucketPPRRepositoryTriggerFilter repositoryTriggerFilter = new BitBucketPPRRepositoryTriggerFilter(
          repositoryPushActionFilter);
      triggers = new ArrayList<>();
      triggers.add(repositoryTriggerFilter);
    }

    return this;
  }

  /**
   * Called when a POST is made.
   * 
   * @param scmTrigger
   * 
   * @throws IOException
   */
  public void onPost(BitBucketPPRHookEvent bitbucketEvent, BitBucketPPRAction bitbucketAction, SCM scmTrigger,
      BitBucketPPRObservable observable) throws Exception {
    logger.log(Level.INFO, "Called onPost with " + bitbucketAction.toString());

    BitBucketPPRFilterMatcher filterMatcher = new BitBucketPPRFilterMatcher();
    List<BitBucketPPRTriggerFilter> matchingFilters = filterMatcher.getMatchingFilters(bitbucketEvent, triggers);

    if (matchingFilters != null && !matchingFilters.isEmpty()) {
      logger.log(Level.INFO, "matchingFilters is not null AND is not empty {0} ", matchingFilters);

      BitBucketPPRPollingRunnable bitbucketPollingRunnable = new BitBucketPPRPollingRunnable(job, getLogFile(),
          new BitBucketPPRPollResultListener() {
            @Override
            public void onPollSuccess(PollingResult pollingResult) {

              logger.log(Level.INFO, "Called onPollSuccess with polling result {0}", pollingResult);
              for (BitBucketPPRTriggerFilter filter : matchingFilters) {
                BitBucketPPRTriggerCause cause;
                try {
                  cause = filter.getCause(getLogFile(), bitbucketAction, bitbucketEvent);

                  logger.log(Level.INFO, () -> "On Poll Success, get cause: " + cause.toString());

                  if (shouldScheduleJob(filter, pollingResult, bitbucketAction)) {
                    scheduleJob(cause, bitbucketAction, scmTrigger, observable, filter);
                    return;
                  }
                } catch (Throwable e) {
                  logger.log(Level.INFO, "Something went wrong in the on Poll Success " + e.getMessage());
                  e.printStackTrace();
                }
              }
            }

            @Override
            public void onPollError(Throwable e) {
              logger.log(Level.INFO, "Called onPollError: " + e.getMessage());
              e.printStackTrace();
            }
          });

      try {
        getDescriptor().queue.execute(bitbucketPollingRunnable);
      } catch (Throwable e) {
        logger.log(Level.INFO, "No matching filters {0}", e.getMessage());
        e.printStackTrace();
      }

    } else {
      logger.log(Level.INFO, "No matching filters");
    }
  }

  private boolean shouldScheduleJob(BitBucketPPRTriggerFilter filter, PollingResult pollingResult,
      BitBucketPPRAction bitbucketAction) {
    logger.log(Level.INFO,
        "Should schedule job: {0} and (polling result has changes: {1} or trigger also if there aren't changes: {2})",
        new Object[] { filter.shouldScheduleJob(bitbucketAction), pollingResult.hasChanges(),
            filter.shouldTriggerAlsoIfNothingChanged() });

    return filter.shouldScheduleJob(bitbucketAction)
        && (pollingResult.hasChanges() || filter.shouldTriggerAlsoIfNothingChanged());
  }

  private void scheduleJob(BitBucketPPRTriggerCause cause, BitBucketPPRAction bitbucketAction, SCM scmTrigger,
      BitBucketPPRObservable observable, BitBucketPPRTriggerFilter filter) {

    if (job == null) {
      logger.warning(() -> "Error: the job is null");
      return;
    }
    ParameterizedJobMixIn<?, ?> pJob = new ParameterizedJobMixIn() {
      @Override
      protected Job<?, ?> asJob() {
        return job;
      }
    };
    logger.info("Check if job should be triggered due to changes in SCM");

    QueueTaskFuture<?> future = pJob.scheduleBuild2(5, new CauseAction(cause), bitbucketAction);

    int buildNumber = job.getNextBuildNumber();
    logger.info(() -> "SCM changes detected in " + job.getName() + ". Triggering " + " #" + buildNumber);

    if (future != null) {
      try {
        future.waitForStart();

        try {
          observable.notifyObservers(BitBucketPPREventFactory.createEvent(BitBucketPPREventType.BUILD_STARTED,
              new BitBucketPPREventContext(bitbucketAction, scmTrigger, job, buildNumber, filter)));
        } catch (Throwable e) {
          logger.info(e.getMessage());
          e.printStackTrace();
        }

        Run<?, ?> run = (Run<?, ?>) future.get();
        if (future.isDone()) {
          try {
            observable.notifyObservers(BitBucketPPREventFactory.createEvent(BitBucketPPREventType.BUILD_FINISHED,
                new BitBucketPPREventContext(bitbucketAction, scmTrigger, run, filter)));
          } catch (Throwable e) {
            logger.info(e.getMessage());
            e.printStackTrace();
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        logger.info(e.getMessage());
        e.printStackTrace();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public Collection<? extends hudson.model.Action> getProjectActions() {
    return Collections.singleton(new BitBucketPPRWebHookPollingAction());
  }

  /**
   * Returns the file that records the last/current polling activity.
   * 
   * @throws JobNotStartedException, IOException
   */
  public File getLogFile() throws JobNotStartedException, IOException {

    if (job == null) {
      throw new JobNotStartedException("No job started");
    }

    File file = new File(job.getRootDir(), "bitbucket-polling.log");
    if (file.createNewFile()) {
      logger.log(Level.FINE, "Created new file {0} for logging in the directory {1}.",
          new Object[] { "bitbucket-polling.log", job.getRootDir() });
    }

    return file;
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  public List<BitBucketPPRTriggerFilter> getTriggers() {
    return triggers;
  }

  /**
   * Action object for {@link BitBucketPPRProject}. Used to display the polling
   * log.
   */
  public class BitBucketPPRWebHookPollingAction implements hudson.model.Action {
    public Job<?, ?> getOwner() {
      return job;
    }

    @Override
    public String getIconFileName() {
      return "clipboard.png";
    }

    @Override
    public String getDisplayName() {
      return "BitBucket Push and Pull Request Hook Log";
    }

    @Override
    public String getUrlName() {
      return "BitBucketPPRPollLog";
    }

    public String getLog() throws Exception {
      return Util.loadFile(getLogFile(), StandardCharsets.UTF_8);
    }

    /**
     * Writes the annotated log to the given output.
     */
    public void writeLogTo(XMLOutput out) throws Exception {
      new AnnotatedLargeText<BitBucketPPRWebHookPollingAction>(getLogFile(), Charset.defaultCharset(), true, this)
          .writeHtmlTo(0, out.asWriter());
    }
  }

  @Symbol("bitBucketTrigger")
  @Extension
  public static class DescriptorImpl extends TriggerDescriptor {
    private final SequentialExecutionQueue queue = new SequentialExecutionQueue(
        Jenkins.MasterComputer.threadPoolForRemoting);

    @Override
    public boolean isApplicable(Item item) {
      return item instanceof Job && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null
          && item instanceof ParameterizedJobMixIn.ParameterizedJob;
    }

    @Override
    public String getDisplayName() {
      return "Build with BitBucket Push and Pull Request Plugin";
    }

    public List<BitBucketPPRTriggerFilterDescriptor> getTriggerDescriptors() {
      // you may want to filter this list of descriptors here, if you are being very
      // fancy
      return Jenkins.get().getDescriptorList(BitBucketPPRTriggerFilter.class);
    }
  }
}
