/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2021, CloudBees, Inc.
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
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import org.apache.commons.jelly.XMLOutput;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.Build;
import hudson.model.queue.QueueTaskFuture;
import hudson.plugins.git.RevisionParameterAction;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.ListBoxModel;
import hudson.util.SequentialExecutionQueue;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRUtils;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventType;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRFilterMatcher;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilter;
import io.jenkins.plugins.bitbucketpushandpullrequest.filter.BitBucketPPRTriggerFilterDescriptor;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRProject;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;

/**
 * 
 * @author cdelmonte
 *
 */
public class BitBucketPPRTrigger extends Trigger<Job<?, ?>> {

  private static final String BITBUCKET_POLLING_LOG = "bitbucket-polling.log";
  private static final Logger logger = Logger.getLogger(BitBucketPPRTrigger.class.getName());
  private List<BitBucketPPRTriggerFilter> triggers;
  public static final boolean ALLOW_HOOKURL_OVERRIDE = true;
  public String credentialsId;

  @DataBoundConstructor
  public BitBucketPPRTrigger(List<BitBucketPPRTriggerFilter> triggers) {
    this.triggers = triggers;
  }

  @DataBoundSetter
  public void setCredentialsId(@CheckForNull String credentialsId) {
    this.credentialsId = credentialsId;
  }

  /**
   * Called when a POST is made.
   * 
   * @param bitbucketEvent
   * @param bitbucketAction
   * @param scmTrigger
   * @param observable
   * @throws Exception
   */
  public void onPost(BitBucketPPRHookEvent bitbucketEvent, BitBucketPPRAction bitbucketAction,
      SCM scmTrigger, BitBucketPPRObservable observable) throws Exception {
    logger.finest(String.format("Called onPost Method for action %s", bitbucketAction));

    if (job == null) {
      logger.warning("Error: the job is null");
      return;
    }

    BitBucketPPRFilterMatcher filterMatcher = new BitBucketPPRFilterMatcher();
    List<BitBucketPPRTriggerFilter> matchingFilters =
        filterMatcher.getMatchingFilters(bitbucketEvent, triggers);

    if (matchingFilters != null && !matchingFilters.isEmpty()) {

      BitBucketPPRPollingRunnable bitbucketPollingRunnable =
          new BitBucketPPRPollingRunnable(job, getLogFile(), new BitBucketPPRPollResultListener() {

            @Override
            public void onPollSuccess(PollingResult pollingResult) {
              matchingFilters.stream().forEach(filter -> {
                try {
                  BitBucketPPRTriggerCause cause =
                      filter.getCause(getLogFile(), bitbucketAction, bitbucketEvent);
                  if (shouldScheduleJob(filter, pollingResult, bitbucketAction)) {
                    scheduleJob(cause, bitbucketAction, scmTrigger, observable, filter);
                  }
                } catch (Throwable e) {
                  logger.warning(String.format(
                      "During the polling process an exception was thrown: %s.", e.getMessage()));
                  e.printStackTrace();
                }
              });
            }

            @Override
            public void onPollError(Throwable e) {
              logger.warning(String.format("Called onPollError: %s.", e.getMessage()));
              e.printStackTrace();
            }
          });

      try {
        getDescriptor().queue.execute(bitbucketPollingRunnable);
      } catch (Throwable e) {
        logger.warning(String.format(
            "Error: cannot add the BB PPR polling runnable to the Jenkins' SequentialExecutionQueue queue: %s",
            e.getMessage()));
        e.printStackTrace();
      }

    } else {
      logger.warning("Triggers are not configured.");
    }
  }

  private boolean shouldScheduleJob(BitBucketPPRTriggerFilter filter, PollingResult pollingResult,
      BitBucketPPRAction bitbucketAction) {
    logger.finest(String.format(
        "Should schedule job: %s and (polling result has changes: %s or trigger also if there aren't changes: %s)",
        filter.shouldScheduleJob(bitbucketAction), pollingResult.hasChanges(),
        filter.shouldTriggerAlsoIfNothingChanged()));

    return filter.shouldScheduleJob(bitbucketAction)
        && (pollingResult.hasChanges() || filter.shouldTriggerAlsoIfNothingChanged());
  }

  private void scheduleJob(BitBucketPPRTriggerCause cause, BitBucketPPRAction bitbucketAction,
      SCM scmTrigger, BitBucketPPRObservable observable, BitBucketPPRTriggerFilter filter)
      throws URISyntaxException {

    // Jenkins will take all instances of QueueAction from this job and will try to compare these
    // instances
    // to all instances of QueueAction from all other pending jobs
    // while calling scheduleBuild2 (see hudson.model.Queue.scheduleInternal)
    // So we need RevisionParameterAction to distinguish THIS job from all other pending jobs in
    // queue

    Queue.Item item = ParameterizedJobMixIn.scheduleBuild2(job, 5, new CauseAction(cause),
        bitbucketAction, new RevisionParameterAction(bitbucketAction.getLatestCommit(),
            new URIish(bitbucketAction.getScmUrls().get(0))));

    QueueTaskFuture<? extends Run<?, ?>> f =
        item != null ? (QueueTaskFuture) item.getFuture() : null;

    if (f == null)
      return;

    try {
      Run<?, ?> startedBuild = (Run<?, ?>) f.waitForStart();

      logger.info(String.format("Triggering %s # %d", job.getName(), startedBuild.getNumber()));

      observable.notifyObservers(BitBucketPPREventFactory.createEvent(
          BitBucketPPREventType.BUILD_STARTED,
          new BitBucketPPREventContext(this, bitbucketAction, scmTrigger, startedBuild, filter)));

      Run<?, ?> run = (Run<?, ?>) f.get();

      if (f.isDone()) {
        observable.notifyObservers(
            BitBucketPPREventFactory.createEvent(BitBucketPPREventType.BUILD_FINISHED,
                new BitBucketPPREventContext(this, bitbucketAction, scmTrigger, run, filter)));
      }

    } catch (InterruptedException | ExecutionException e) {
      logger.warning(e.getMessage());
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  File getLogFile() {
    if (job == null)
      return null;
    return new File(job.getRootDir(), BITBUCKET_POLLING_LOG);
  }

  @Override
  public Collection<? extends hudson.model.Action> getProjectActions() {
    return Collections.singleton(new BitBucketPPRWebHookPollingAction());
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  public List<BitBucketPPRTriggerFilter> getTriggers() {
    return triggers;
  }

  /**
   * Action object for {@link BitBucketPPRProject}. Used to display the polling log.
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
      new AnnotatedLargeText<BitBucketPPRWebHookPollingAction>(getLogFile(),
          Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
    }
  }

  @Symbol("bitBucketTrigger")
  @Extension
  public static class DescriptorImpl extends TriggerDescriptor {
    private final SequentialExecutionQueue queue =
        new SequentialExecutionQueue(Jenkins.MasterComputer.threadPoolForRemoting);

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context,
        @QueryParameter String remote, @QueryParameter String credentialsId) {

      if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
          || context != null && !context.hasPermission(Item.EXTENDED_READ)) {
        return new StandardListBoxModel().includeCurrentValue(credentialsId);
      }

      return new StandardListBoxModel().includeEmptyValue()
          .includeMatchingAs(ACL.SYSTEM, Jenkins.getInstance(), StandardCredentials.class,
              Collections.<DomainRequirement>emptyList(), CredentialsMatchers.always())
          .includeCurrentValue(credentialsId);
    }

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
