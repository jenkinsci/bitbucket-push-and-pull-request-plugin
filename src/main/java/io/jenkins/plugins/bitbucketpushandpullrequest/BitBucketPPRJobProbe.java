/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2025, Christian Del Monte.
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

import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMHead;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import jenkins.branch.BranchSource;
import jenkins.scm.api.SCMSource;

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.*;

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

    Function<String, URIish> makeUrl = a -> {
      try {
        return new URIish(a);
      } catch (URISyntaxException e) {
        logger.warning(String.format("Invalid URI %s.", e.getMessage()));
        return null;
      }
    };


    List<URIish> remoteScmUrls = bitbucketAction.getScmUrls().stream().map(makeUrl)
        .filter(Objects::nonNull).collect(Collectors.toList());

    try (ACLContext ctx = ACL.as2(ACL.SYSTEM2)) {
      if (globalConfig.isSingleJobSet()) {
        try {
          Job job = (Job) Jenkins.get().getItemByFullName(globalConfig.getSingleJob());
          if (job == null) {
            logger.log(Level.WARNING, "Job could not be found!");
            return;
          }
          triggerScmForSingleJob(job, remoteScmUrls, bitbucketEvent, bitbucketAction, observable);
        } catch (TriggerNotSetException e) {
          logger.log(Level.FINE, "Trigger not set");
        }
      } else {
        Jenkins.get().getAllItems(Job.class).forEach(job -> {
          try {
            triggerScm(job, remoteScmUrls, bitbucketEvent, bitbucketAction, observable);
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

    Trigger jobTrigger = new Trigger(getBitBucketTrigger(job)
            .orElseThrow(() -> new TriggerNotSetException(job.getFullDisplayName())), Optional.ofNullable(SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job)));

    jobTrigger.scmTriggerItem.ifPresent(it -> it.getSCMs().forEach(scm -> {

      // Single-job mode does not match webhook URLs against the job's SCM (the job is
      // pre-selected by the caller), but the Pipeline-shared-library exclusion still
      // applies: a library SCM that happens to be in this job's getSCMs() must not
      // become an onPost target (issue #380).
      if (scm instanceof GitSCM && isExcludedAsPipelineLibrary(job, (GitSCM) scm)) {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE,
              "Skipping SCM for single-job {0}: classified as a Pipeline shared library.",
              job.getName());
        }
        return;
      }

      if (!scmTriggered.contains(scm)) {
        scmTriggered.add(scm);

        try {
          jobTrigger.bitbucketTrigger.onPost(bitbucketEvent, bitbucketAction, scm, observable);
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

    Trigger jobTrigger = new Trigger(getBitBucketTrigger(job)
            .orElseThrow(() -> new TriggerNotSetException(job.getFullDisplayName())), Optional.ofNullable(SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job)));

    jobTrigger.scmTriggerItem.ifPresent(it -> it.getSCMs().forEach(scm -> {

      triggerMultibranchScan(job, bitbucketAction);

      // @todo add comments to explain what is this check for
      if (job.getParent() instanceof MultiBranchProject
          && mPJobShouldNotBeTriggered(job, bitbucketEvent, bitbucketAction)) {
        logger.log(Level.FINEST, "Skipping job {0}.", job.getDisplayName());
        return;
      }

      if (!(scm instanceof GitSCM)) {
        return;
      }
      GitSCM gitScm = (GitSCM) scm;

      // Filter ordering: match the webhook URL against the SCM FIRST. The library
      // checks depend only on (job, scm) — running them inside the per-URI stream
      // would re-evaluate them for every URI in `remotes`, and running them at all
      // for jobs whose SCM URL does not even overlap the webhook payload wastes work
      // on every unrelated job in a large Jenkins instance.
      if (remotes.stream().noneMatch(url -> matchGitScm(gitScm, url))) {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "{0} SCM doesn't match remote repo {1}.",
              new Object[] {job.getName(),
                  remotes.stream().map(URIish::toString).collect(Collectors.joining(", "))});
        }
        return;
      }

      if (isExcludedAsPipelineLibrary(job, gitScm)) {
        return;
      }

      if (!scmTriggered.contains(gitScm)) {
        scmTriggered.add(gitScm);

        try {
          jobTrigger.bitbucketTrigger.onPost(bitbucketEvent, bitbucketAction, gitScm, observable);
          return;

        } catch (Exception e) {
          logger.log(Level.WARNING, "Error: {0}", e.getMessage());
        }
      }

      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "{0} SCM was already triggered for remote repo {1}.",
            new Object[] {job.getName(),
                remotes.stream().map(URIish::toString).collect(Collectors.joining(", "))});
      }

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
        // For PR events, check if this job is associated with the PR's source branch
        // via the SCMHead. This works regardless of the job's display name which may
        // be set to the PR title by bitbucket-branch-source plugin.
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        if (head != null) {
          String headName = head.getName();
          if (sourceBranchName.equalsIgnoreCase(headName)) {
            return false; // trigger this job
          }
          return true; // different branch/PR, skip
        }

        // Fallback for jobs without an SCMHead: exact match on display name
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
    if (job instanceof ParameterizedJobMixIn.ParameterizedJob<?, ?> pJob) {

        return pJob.getTriggers().values().stream().filter(BitBucketPPRTrigger.class::isInstance)
          .findFirst().map(BitBucketPPRTrigger.class::cast);
    }
    return Optional.empty();
  }


  // Two-counter scan budget:
  //
  // RECENT_BUILDS_WINDOW bounds the *semantic* lookback — how many builds with
  // an informative-but-UNKNOWN verdict we are willing to look past before
  // giving up. Pre-upgrade builds (action == null) and in-progress builds are
  // skipped and do NOT consume this budget; otherwise their noise would mask
  // the first informative verdict.
  //
  // MAX_BUILDS_TO_SCAN bounds the *operational* lookback — the absolute
  // number of Run objects the scan will walk through, regardless of their
  // state. Pre-upgrade and in-progress builds do not consume the semantic
  // budget, but they still consume MAX_BUILDS_TO_SCAN. A job with thousands of
  // pre-upgrade builds would otherwise let the semantic counter alone walk
  // arbitrarily far; this cap keeps the per-webhook cost finite.
  private static final int RECENT_BUILDS_WINDOW = 10;
  private static final int MAX_BUILDS_TO_SCAN = 50;

  // Together with isRecordedOnlyAsPipelineLibrary, this method participates in
  // the Pipeline-shared-library exclusion: both gates are always evaluated and
  // either returning true skips the SCM. isLibrarySCM is the older, narrower
  // heuristic (single-remote with a non-origin remote name); it is retained
  // because it still covers cases the listener cannot: builds executed before
  // this plugin version (no recorded action) and jobs whose recent builds did
  // not exercise the library code path (action absent from the entire window).
  // Do NOT remove this method: deleting it would silently reintroduce #281 for
  // those jobs. Do NOT extend it either — remote names are not a reliable
  // signal for Pipeline shared libraries (see #378 and #380); for new shapes,
  // extend BitBucketPPRSCMCheckoutListener / BitBucketPPRPipelineLibrarySCMAction.
  private boolean isLibrarySCM(SCM scm) {
    if (!(scm instanceof GitSCM)) {
      return false;
    }
    GitSCM gitScm = (GitSCM) scm;
    List<RemoteConfig> repositories = gitScm.getRepositories();

    // A multi-remote GitSCM is a valid Jenkins configuration and must not be
    // classified as a shared library solely from remote names. JGit may expose
    // multiple remotes as origin, origin1, origin2, ... at runtime, which would
    // make the non-origin heuristic skip the entire SCM before URL matching
    // has a chance to run (issue #378).
    if (repositories.size() != 1) {
      return false;
    }

    String remoteName = repositories.get(0).getName();
    if (remoteName != null && !remoteName.isEmpty() && !"origin".equals(remoteName)) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE,
            "Skipping SCM with remote name ''{0}'' as it appears to be a shared library.",
            remoteName);
      }
      return true;
    }
    return false;
  }

  // Returns true if `scm` is recognised as a Pipeline shared library for `job`,
  // either by the legacy remote-name heuristic (isLibrarySCM) or by an entry
  // recorded ONLY as a library (not also as an explicit checkout) by
  // BitBucketPPRSCMCheckoutListener within the recent-builds window.
  private boolean isExcludedAsPipelineLibrary(Job<?, ?> job, GitSCM scm) {
    return isLibrarySCM(scm) || isRecordedOnlyAsPipelineLibrary(job, scm);
  }

  // Scans recent builds for an authoritative verdict from
  // BitBucketPPRPipelineLibrarySCMAction.classify(scm). The scan stops on the
  // FIRST build whose classify is informative:
  //   ONLY_LIBRARY         -> return true  (skip the SCM)
  //   NON_LIBRARY_OR_MIXED -> return false (do NOT skip — the most recent build
  //                                         with an opinion says the SCM is
  //                                         consumed as a real source too)
  //   UNKNOWN              -> keep scanning earlier builds
  //
  // The tri-state is what prevents an older "library-only" snapshot from
  // shadowing a newer "library+explicit" snapshot — collapsing to a boolean
  // would return true on the older entry and silently filter a webhook the
  // user expects to fire (issue #380 corollary).
  //
  // In-progress builds are skipped: their action reflects whatever checkouts
  // have completed so far and can return ONLY_LIBRARY prematurely (e.g. the
  // library was checked out at the top of the Jenkinsfile but the explicit
  // checkout step has not run yet). Pre-upgrade builds (action == null)
  // contribute nothing and also do not consume the semantic window.
  //
  // Two budgets bound the walk: see RECENT_BUILDS_WINDOW / MAX_BUILDS_TO_SCAN.
  private boolean isRecordedOnlyAsPipelineLibrary(Job<?, ?> job, GitSCM scm) {
    int informativeRemaining = RECENT_BUILDS_WINDOW;
    int scannedRemaining = MAX_BUILDS_TO_SCAN;
    for (Run<?, ?> run = job.getLastBuild();
         run != null && informativeRemaining > 0 && scannedRemaining > 0;
         run = run.getPreviousBuild(), scannedRemaining--) {
      if (run.isBuilding()) {
        continue;
      }
      BitBucketPPRPipelineLibrarySCMAction action =
          run.getAction(BitBucketPPRPipelineLibrarySCMAction.class);
      if (action == null) {
        continue;
      }
      switch (action.classify(scm)) {
        case ONLY_LIBRARY:
          if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,
                "Skipping SCM {0} as it was recorded exclusively as a Pipeline shared library on build {1}.",
                new Object[] {scm.getKey(), run.getFullDisplayName()});
          }
          return true;
        case NON_LIBRARY_OR_MIXED:
          return false;
        case UNKNOWN:
        default:
          informativeRemaining--;
          break;
      }
    }
    return false;
  }

  private boolean matchGitScm(SCM scm, URIish remote) {
    return ((GitSCM) scm).getRepositories().stream()
        .anyMatch((repo) -> repo.getURIs().stream().anyMatch((repoUrl) -> GitStatus.looselyMatches(repoUrl, remote)));
  }

  private void triggerMultibranchScan(@Nonnull Job<?, ?> job,
                                      BitBucketPPRAction bitbucketAction) {

      String getLatestCommit = bitbucketAction.getLatestCommit();
      String getLatestFromCommit = bitbucketAction.getLatestFromCommit();
      String pipelineName = job.getParent().getFullName();
      String getPayldChgType = bitbucketAction.getPayloadChangeType();

      if ((getLatestCommit != null) && (getLatestFromCommit != null) && (pipelineName != null) && (getPayldChgType != null)) {
        if ((getLatestFromCommit.equals(EMPTY_HASH) && PAYLOAD_CHANGE_TYPE_ADD.equals(getPayldChgType)) ||
            (getLatestCommit.equals(EMPTY_HASH) && PAYLOAD_CHANGE_TYPE_DELETE.equals(getPayldChgType))) {

            Jenkins jenkins = Jenkins.get();

            WorkflowMultiBranchProject mbp = jenkins.getInstance().getItemByFullName(pipelineName, WorkflowMultiBranchProject.class);

            if (mbp != null) {
              for (BranchSource bs : mbp.getSourcesList()) {
                SCMSource src = bs.getSource();

                logger.log(Level.FINEST,
                      "Source Type: {0}",
                      new String[] { src.getDescriptor().getDisplayName() });

                if (src instanceof jenkins.plugins.git.GitSCMSource) {
                  jenkins.plugins.git.GitSCMSource git = (jenkins.plugins.git.GitSCMSource) src;
                  String gitRemote = git.getRemote();

                  logger.log(Level.FINEST,
                      "Branch Source URL: {0}",
                      new String[] { gitRemote });

                  List<String> cloneUrls = bitbucketAction.getScmUrls();
                  if (cloneUrls != null && cloneUrls.contains(gitRemote)) {
                    logger.log(Level.FINEST,
                          "Branch Source URL: {0}, clone URLs: {1}",
                          new Object[] { gitRemote, cloneUrls });

                    mbp.scheduleBuild2(0);

                    logger.log(Level.INFO,
                      "Triggered branch indexing for: {0}",
                      new String[] { pipelineName });
                  }
                }
              }
            } else {
                logger.log(Level.WARNING,
                      "Multibranch job not found: {0}",
                      new String[] { pipelineName });
            }
        }
      }
  }
}
