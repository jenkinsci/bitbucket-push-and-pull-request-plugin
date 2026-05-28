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

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jenkins.model.ParameterizedJobMixIn;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

/**
 * Records, at checkout time, the role each Git checkout played in a build — Pipeline shared
 * library vs. ordinary (non-library) checkout — so that {@link BitBucketPPRJobProbe} can later
 * skip webhook events for repositories that the build only consumed as a library (issue #380),
 * while still letting webhooks trigger when the same repository was also pulled in via an explicit
 * {@code checkout} step.
 *
 * <p>A checkout is identified as a shared-library checkout by the workspace layout used by
 * {@code pipeline-groovy-lib}'s legacy {@code SCMRetriever}:
 * {@code <job-workspace>@libs/<directoryName>}. The {@code @libs} marker must be followed by a
 * path separator and at least one more character; a path that merely ends in {@code @libs} (no
 * trailing segment) is not recognised, since that shape collides with user-chosen job names
 * ending in {@code @libs}.
 *
 * <p>Scope: the listener is global (Jenkins wires {@link SCMListener}s in before this plugin
 * knows which jobs care), but it only records checkouts for jobs that have a
 * {@link BitBucketPPRTrigger} configured. Builds on unrelated jobs are not annotated, so
 * uninstalling the plugin does not leave orphan action references in their {@code build.xml}.
 *
 * <p>Known limitations:
 *
 * <ul>
 *   <li>Clone-mode shared libraries (workspace path
 *       {@code <build-root>/libs/<directoryName>}) are not detected on distributed builds,
 *       because the build-root is a controller-side path while the checkout workspace lives on
 *       the agent.
 *   <li>Triggers defined inside the {@code Jenkinsfile} via {@code properties { pipelineTriggers
 *       ... }} are not present on the job at the time of the very first checkout, so the very
 *       first build under this plugin version may not be protected for that job; subsequent
 *       builds are. For multibranch projects this means each newly-discovered branch is
 *       unprotected on its first build (the legacy remote-name heuristic in
 *       {@code BitBucketPPRJobProbe.isLibrarySCM} still catches the non-origin variant).
 * </ul>
 */
@Extension
public class BitBucketPPRSCMCheckoutListener extends SCMListener {

  private static final Logger LOGGER =
      Logger.getLogger(BitBucketPPRSCMCheckoutListener.class.getName());

  // Pipeline-groovy-lib's legacy retriever checks shared libraries out under
  // <job-workspace>@libs/<directoryName>[/...]. The `@libs` token must be
  // followed by a path separator AND at least one more character; a bare
  // `...@libs` suffix is NOT matched (collides with user job names ending in @libs).
  private static final Pattern PIPELINE_LIBRARY_WORKSPACE =
      Pattern.compile(".*@libs[/\\\\].+");

  @Override
  public void onCheckout(
      Run<?, ?> build,
      SCM scm,
      FilePath workspace,
      TaskListener listener,
      File changelogFile,
      SCMRevisionState pollingBaseline) {
    if (build == null || workspace == null || !(scm instanceof GitSCM)) {
      return;
    }
    if (!jobUsesBitBucketTrigger(build.getParent())) {
      return;
    }

    GitSCM gitScm = (GitSCM) scm;
    boolean asLibrary = isPipelineLibraryWorkspace(workspace);

    // Extract URIs ONCE, before allocating/attaching the action, so that a
    // malformed GitSCM does not leave an empty action behind on a build that
    // legitimately had no recordable URIs — and so that the action is only
    // touched in one place (here), keeping the GitSCM access path trivially
    // auditable.
    List<URIish> uris;
    try {
      uris = collectUris(gitScm);
    } catch (RuntimeException e) {
      LOGGER.log(
          Level.WARNING,
          "Failed to extract URIs from GitSCM on build "
              + build.getFullDisplayName()
              + "; continuing without library-filter protection for this checkout",
          e);
      return;
    }
    if (uris.isEmpty()) {
      return;
    }

    BitBucketPPRPipelineLibrarySCMAction action;
    // Synchronize on the Run so concurrent onCheckout calls (parallel-stage
    // checkouts) cannot both pass the null-check and install duplicate actions —
    // Run.getAction(Class) would then return only the first, dropping every URI
    // recorded into the second instance.
    synchronized (build) {
      action = build.getAction(BitBucketPPRPipelineLibrarySCMAction.class);
      if (action == null) {
        action = new BitBucketPPRPipelineLibrarySCMAction();
        build.addAction(action);
      }
    }

    if (asLibrary) {
      action.recordLibrary(uris);
    } else {
      action.recordNonLibrary(uris);
    }

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(
          Level.FINE,
          "Recorded {0} checkout for SCM {1} on build {2}",
          new Object[] {asLibrary ? "Pipeline library" : "non-library",
              gitScm.getKey(), build.getFullDisplayName()});
    }
  }

  /**
   * Returns true if {@code job} has a {@link BitBucketPPRTrigger} configured. Mirrors the probe's
   * {@code getBitBucketTrigger(job)}: jobs the probe would process are exactly the jobs the
   * listener records for.
   */
  private static boolean jobUsesBitBucketTrigger(Job<?, ?> job) {
    if (!(job instanceof ParameterizedJobMixIn.ParameterizedJob<?, ?>)) {
      return false;
    }
    return ((ParameterizedJobMixIn.ParameterizedJob<?, ?>) job)
        .getTriggers().values().stream().anyMatch(BitBucketPPRTrigger.class::isInstance);
  }

  static boolean isPipelineLibraryWorkspace(FilePath workspace) {
    if (workspace == null) {
      return false;
    }
    String remote = workspace.getRemote();
    return remote != null && PIPELINE_LIBRARY_WORKSPACE.matcher(remote).matches();
  }

  private static List<URIish> collectUris(GitSCM scm) {
    List<URIish> out = new ArrayList<>();
    for (RemoteConfig repo : scm.getRepositories()) {
      List<URIish> uris = repo.getURIs();
      if (uris == null) {
        continue;
      }
      for (URIish uri : uris) {
        if (uri != null) {
          out.add(uri);
        }
      }
    }
    return out;
  }
}
