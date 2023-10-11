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

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_MERGED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.PULL_REQUEST_SERVER_MERGED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_CLOUD_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.REPOSITORY_SERVER_PUSH;

import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.TriggerNotSetException;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.eclipse.jgit.transport.URIish;

/**
 * 
 * @author cdelmonte
 *
 */
public class BitBucketPPRJobProbe {
  private static final Logger logger = Logger.getLogger(BitBucketPPRJobProbe.class.getName());

  private static final BitBucketPPRPluginConfig globalConfig =
          BitBucketPPRPluginConfig.getInstance();
  private final List<SCM> scmTriggered;

  public BitBucketPPRJobProbe() {
    scmTriggered = new ArrayList<>();
  }

  public void triggerMatchingJobs(BitBucketPPRHookEvent bitbucketEvent,
      BitBucketPPRAction bitbucketAction, BitBucketPPRObservable observable) {

    // @todo deprecated. It will be removed in v3.0
    if (!("git".equals(bitbucketAction.getScm()) || "hg".equals(bitbucketAction.getScm()))) {
      throw new UnsupportedOperationException(
          String.format("Unsupported SCM type %s", bitbucketAction.getScm()));
    }

    Function<String, URIish> f = a -> {
      try {
        return new URIish(a);
      } catch (URISyntaxException e) {
        logger.warning(String.format("Invalid URI %s.", e.getMessage()));
        return null;
      }
    };
    List<URIish> remotes = bitbucketAction.getScmUrls().stream().map(f)
        .filter(Objects::nonNull).collect(Collectors.toList());

    try (ACLContext ctx = ACL.as(ACL.SYSTEM)) {
      if (globalConfig.isSingleJobSet()) {
        try {
          Job job = (Job) Jenkins.get().getItemByFullName(globalConfig.getSingleJob());
          if (job == null) {
            logger.log(Level.WARNING, "Job could not be found!");
            return;
          }
          triggerScmForSingleJob(job, remotes, bitbucketEvent, bitbucketAction, observable);
        } catch (TriggerNotSetException e) {
          logger.log(Level.FINE, "Trigger not set");
        }
      } else {
        Jenkins.get().getAllItems(Job.class).forEach(job -> {
          try {
            triggerScm(job, remotes, bitbucketEvent, bitbucketAction, observable);
          } catch (TriggerNotSetException e) {
            logger.log(Level.FINE, "Trigger not set");
          }
        });
      }
    }
  }

  private void triggerScmForSingleJob(@Nonnull Job<?, ?> job, List<URIish> remotes,
                          BitBucketPPRHookEvent bitbucketEvent, BitBucketPPRAction bitbucketAction,
                          BitBucketPPRObservable observable) throws TriggerNotSetException {

    Trigger trigger = new Trigger(getBitBucketTrigger(job)
            .orElseThrow(() -> new TriggerNotSetException(job.getFullDisplayName())), Optional.ofNullable(SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job)));

    trigger.scmTriggerItem.ifPresent(it -> it.getSCMs().forEach(scmTrigger -> {

      if (!scmTriggered.contains(scmTrigger)) {
        scmTriggered.add(scmTrigger);

        try {
          trigger.bitbucketTrigger.onPost(bitbucketEvent, bitbucketAction, scmTrigger, observable);
          return;

        } catch (Exception e) {
          logger.log(Level.WARNING, "Error: {0}", e.getMessage());
        }
      }

      logger.log(Level.FINE, "{0} SCM doesn't match remote repo {1} or it was already triggered.",
              new Object[] {job.getName(), remotes});

    }));
  }

  private void triggerScm(@Nonnull Job<?, ?> job, List<URIish> remotes,
      BitBucketPPRHookEvent bitbucketEvent, BitBucketPPRAction bitbucketAction,
      BitBucketPPRObservable observable) throws TriggerNotSetException {

    Trigger trigger = new Trigger(getBitBucketTrigger(job)
            .orElseThrow(() -> new TriggerNotSetException(job.getFullDisplayName())), Optional.ofNullable(SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job)));

    trigger.scmTriggerItem.ifPresent(it -> it.getSCMs().forEach(scmTrigger -> {

      // @todo add comments to explain what is this check for
      if (job.getParent() instanceof MultiBranchProject
          && mPJobShouldNotBeTriggered(job, bitbucketEvent, bitbucketAction)) {
        logger.log(Level.FINEST, "Skipping job {0}.", job.getDisplayName());
        return;
      }

      Predicate<URIish> p = (url) -> scmTrigger instanceof GitSCM && matchGitScm(scmTrigger, url);

      if (remotes.stream().anyMatch(p) && !scmTriggered.contains(scmTrigger)) {
        scmTriggered.add(scmTrigger);

        try {
          trigger.bitbucketTrigger.onPost(bitbucketEvent, bitbucketAction, scmTrigger, observable);
          return;

        } catch (Exception e) {
          logger.log(Level.WARNING, "Error: {0}", e.getMessage());
        }
      }

      logger.log(Level.FINE, "{0} SCM doesn't match remote repo {1} or it was already triggered.",
          new Object[] { job.getName(), remotes.stream().map(URIish::toString).collect(Collectors.joining(", ")) });

    }));
  }

  private static class Trigger {
    public final BitBucketPPRTrigger bitbucketTrigger;
      final Optional<SCMTriggerItem> scmTriggerItem;

    public Trigger(BitBucketPPRTrigger bitbucketTrigger, Optional<SCMTriggerItem> item) {
      this.bitbucketTrigger = bitbucketTrigger;
      this.scmTriggerItem = item;
    }
  }

  private boolean mPJobShouldNotBeTriggered(Job<?, ?> job, BitBucketPPRHookEvent bitbucketEvent,
      BitBucketPPRAction bitbucketAction) {

    if (job.getDisplayName() != null) {
      String displayName = job.getDisplayName();
      String sourceBranchName = bitbucketAction.getSourceBranch();
      String targetBranchName = bitbucketAction.getTargetBranch();

      logger.log(Level.FINEST,
          "Bitbucket event is : {0}, Job Name : {1}, sourceBranchName: {2}, targetBranchName: {3}",
          new String[] {
              bitbucketEvent.getAction(), displayName, sourceBranchName,
              targetBranchName });

      if (PULL_REQUEST_MERGED.equalsIgnoreCase(bitbucketEvent.getAction())) {
        return !displayName.equalsIgnoreCase(targetBranchName);
      }

      if (PULL_REQUEST_SERVER_MERGED.equalsIgnoreCase(bitbucketEvent.getAction())) {
        return !displayName.equalsIgnoreCase(targetBranchName);
      }

      if (sourceBranchName != null) {
        return !displayName.equalsIgnoreCase(sourceBranchName);
      }

      if (REPOSITORY_CLOUD_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())
          && targetBranchName != null) {
        return !displayName.equals(targetBranchName);
      }

      if (REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())
          && targetBranchName != null) {
        return !displayName.equals(targetBranchName);
      }
    }

    return true;
  }

  private Optional<BitBucketPPRTrigger> getBitBucketTrigger(Job<?, ?> job) {
    if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
      ParameterizedJobMixIn.ParameterizedJob<?, ?> pJob =
          (ParameterizedJobMixIn.ParameterizedJob<?, ?>) job;

      return pJob.getTriggers().values().stream().filter(BitBucketPPRTrigger.class::isInstance)
          .findFirst().map(BitBucketPPRTrigger.class::cast);
    }
    return Optional.empty();
  }


  private boolean matchGitScm(SCM scm, URIish remote) {
    return ((GitSCM) scm).getRepositories().stream()
        .anyMatch((a) -> a.getURIs().stream().anyMatch((b) -> GitStatus.looselyMatches(b, remote)));
  }

}
