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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import hudson.model.InvisibleAction;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

/**
 * Records, on a {@link hudson.model.Run}, the Git remotes seen by the build, split by the role
 * they played:
 *
 * <ul>
 *   <li>{@code libraryRemotes} — checkouts done under a Pipeline shared-library workspace
 *       ({@code <job-workspace>@libs/<dir>}, recognised by
 *       {@code BitBucketPPRSCMCheckoutListener}).
 *   <li>{@code nonLibraryRemotes} — every other Git checkout the build performed (the job's main
 *       source SCM, explicit {@code checkout} steps, etc.).
 * </ul>
 *
 * <p>The probe consults {@link #classify(GitSCM)} to decide whether to skip a webhook match.
 * The result is tri-state ({@link Classification#ONLY_LIBRARY}, {@link
 * Classification#NON_LIBRARY_OR_MIXED}, {@link Classification#UNKNOWN}) so the probe can
 * distinguish "this build proves the SCM is exclusively a library" from "this build has no
 * opinion" — the latter is what enables the scan-window logic to fall back to an older build
 * without having a newer build's NON_LIBRARY verdict silently shadowed by an older
 * ONLY_LIBRARY verdict.
 *
 * <p>Stored URIs are sanitized to host + path: that is the only subset {@link
 * GitStatus#looselyMatches(URIish, URIish)} actually consults, and we deliberately do not
 * duplicate scheme/user/password into a separate action on the Run.
 *
 * <p>Thread-safety: writes happen from build executor threads (parallel-stage library/checkout
 * steps fire concurrently) while reads happen from the webhook HTTP dispatch thread; all methods
 * that touch the internal lists are {@code synchronized} on the action instance.
 */
public class BitBucketPPRPipelineLibrarySCMAction extends InvisibleAction {

  /**
   * Per-build verdict on whether a queried {@link GitSCM} was exclusively a Pipeline shared
   * library in this build, also a real (non-library) checkout, or simply not seen.
   *
   * <p>The probe uses this to walk the recent-builds window: an {@code UNKNOWN} verdict means
   * "this build has nothing to say, keep scanning"; the other two are authoritative.
   */
  public enum Classification {
    /** SCM matches a library remote and does NOT also match any non-library remote. */
    ONLY_LIBRARY,
    /** SCM matches a non-library remote, or is multi-remote (never a library by construction). */
    NON_LIBRARY_OR_MIXED,
    /** This build has not seen any of the SCM's URIs in either role. */
    UNKNOWN
  }

  private final List<URIish> libraryRemotes = new ArrayList<>();
  private final List<URIish> nonLibraryRemotes = new ArrayList<>();

  /** Records the given URIs as Pipeline shared-library checkouts. */
  public synchronized void recordLibrary(Iterable<URIish> uris) {
    record(uris, libraryRemotes);
  }

  /** Records the given URIs as non-library (explicit) checkouts. */
  public synchronized void recordNonLibrary(Iterable<URIish> uris) {
    record(uris, nonLibraryRemotes);
  }

  /**
   * Returns this build's verdict on {@code scm}:
   *
   * <ul>
   *   <li>{@link Classification#NON_LIBRARY_OR_MIXED} when {@code scm} is multi-remote (by
   *       construction not a library — preserves #378) OR any of its URIs matches a recorded
   *       non-library URI;
   *   <li>{@link Classification#ONLY_LIBRARY} when at least one URI matches a library URI AND no
   *       URI matches a non-library URI;
   *   <li>{@link Classification#UNKNOWN} when none of the URIs has been seen in this build.
   * </ul>
   *
   * <p>This three-valued return is what allows the probe to honour the most-recent build's
   * verdict instead of OR-ing across the window: a more recent build that returns
   * {@code NON_LIBRARY_OR_MIXED} must shadow an older build that returned {@code ONLY_LIBRARY}.
   *
   * <p>Scoping: the multi-remote shortcut returns {@code NON_LIBRARY_OR_MIXED} unconditionally,
   * including when none of the queried URIs is actually in either recorded list. That is
   * intentional but only meaningful in the probe's call chain — {@code classify} is consulted
   * AFTER {@code BitBucketPPRJobProbe.matchGitScm} has already established that {@code scm}'s
   * URL overlaps the webhook payload, so an irrelevant multi-remote SCM never reaches this
   * method in production. The shortcut is what lets the probe's scan stop at the most recent
   * "this is a real source" verdict instead of regressing to an older library-only entry for
   * one of the multi-remote URIs.
   */
  public synchronized Classification classify(GitSCM scm) {
    if (scm == null) {
      return Classification.UNKNOWN;
    }
    List<RemoteConfig> repos = scm.getRepositories();
    if (repos.size() != 1) {
      // Multi-remote GitSCM is by construction a real source SCM, never a library
      // (preserves the invariant from #378). Returning NON_LIBRARY_OR_MIXED — rather
      // than UNKNOWN — ensures the probe's scan stops here and does not regress to
      // an older build's ONLY_LIBRARY verdict for a remote that coincides with one
      // of the multi-remote URIs.
      return Classification.NON_LIBRARY_OR_MIXED;
    }
    List<URIish> uris = repos.get(0).getURIs();
    if (uris == null) {
      return Classification.UNKNOWN;
    }
    boolean seenAsLibrary = false;
    for (URIish queried : uris) {
      if (queried == null) {
        continue;
      }
      if (matchesAny(queried, nonLibraryRemotes)) {
        return Classification.NON_LIBRARY_OR_MIXED;
      }
      if (matchesAny(queried, libraryRemotes)) {
        seenAsLibrary = true;
      }
    }
    return seenAsLibrary ? Classification.ONLY_LIBRARY : Classification.UNKNOWN;
  }

  private void record(Iterable<URIish> uris, List<URIish> target) {
    if (uris == null) {
      return;
    }
    for (URIish uri : uris) {
      if (uri == null) {
        continue;
      }
      URIish sanitized = sanitize(uri);
      if (!matchesAny(sanitized, target)) {
        target.add(sanitized);
      }
    }
  }

  // GitStatus.looselyMatches consults only host and normalized path; strip the
  // rest so the Run action does not duplicate credentials or scheme that we do
  // not actually need to do the comparison.
  private static URIish sanitize(URIish uri) {
    return uri.setUser(null).setPass(null).setScheme(null).setPort(-1);
  }

  private static boolean matchesAny(URIish uri, List<URIish> list) {
    for (URIish stored : list) {
      if (GitStatus.looselyMatches(stored, uri)) {
        return true;
      }
    }
    return false;
  }
}
