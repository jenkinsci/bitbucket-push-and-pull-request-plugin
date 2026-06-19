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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitBucketPPRJobProbeTest {

  private boolean invokeIsLibrarySCM(BitBucketPPRJobProbe probe, SCM scm) throws Exception {
    Method method = BitBucketPPRJobProbe.class.getDeclaredMethod("isLibrarySCM", SCM.class);
    method.setAccessible(true);
    return (boolean) method.invoke(probe, scm);
  }

  private boolean invokeIsRecordedOnlyAsPipelineLibrary(
      BitBucketPPRJobProbe probe, Job<?, ?> job, GitSCM scm) throws Exception {
    Method method =
        BitBucketPPRJobProbe.class.getDeclaredMethod(
            "isRecordedOnlyAsPipelineLibrary", Job.class, GitSCM.class);
    method.setAccessible(true);
    return (boolean) method.invoke(probe, job, scm);
  }

  private GitSCM mockGitScmWithUrl(String url) throws URISyntaxException {
    GitSCM scm = mock(GitSCM.class);
    RemoteConfig remote = mock(RemoteConfig.class);
    when(remote.getURIs()).thenReturn(List.of(new URIish(url)));
    when(scm.getRepositories()).thenReturn(List.of(remote));
    return scm;
  }

  private static List<URIish> uris(String url) throws URISyntaxException {
    return List.of(new URIish(url));
  }

  @Test
  void testIsLibrarySCMWithSingleOriginRemoteIsNotLibrary() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      GitSCM gitScm = mock(GitSCM.class);
      RemoteConfig remoteConfig = mock(RemoteConfig.class);
      when(remoteConfig.getName()).thenReturn("origin");
      when(gitScm.getRepositories()).thenReturn(List.of(remoteConfig));

      assertFalse(invokeIsLibrarySCM(probe, gitScm));
    }
  }

  // Also acts as regression guard for issue #281: a single-remote SCM whose
  // name is non-origin is still treated as a Pipeline shared-library candidate,
  // preserving the existing filter behavior.
  @Test
  void testIsLibrarySCMWithSingleNonOriginRemoteIsClassifiedAsLibrary() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      GitSCM gitScm = mock(GitSCM.class);
      RemoteConfig remoteConfig = mock(RemoteConfig.class);
      when(remoteConfig.getName()).thenReturn("my-shared-lib");
      when(gitScm.getRepositories()).thenReturn(List.of(remoteConfig));

      assertTrue(invokeIsLibrarySCM(probe, gitScm));
    }
  }

  @Test
  void testIsLibrarySCMWithNullRemoteName() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      GitSCM gitScm = mock(GitSCM.class);
      RemoteConfig remoteConfig = mock(RemoteConfig.class);
      when(remoteConfig.getName()).thenReturn(null);
      when(gitScm.getRepositories()).thenReturn(List.of(remoteConfig));

      assertFalse(invokeIsLibrarySCM(probe, gitScm));
    }
  }

  @Test
  void testIsLibrarySCMWithNonGitSCM() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();
      SCM nonGitScm = mock(SCM.class);

      assertFalse(invokeIsLibrarySCM(probe, nonGitScm));
    }
  }

  // Regression test for issue #378: a multi-remote GitSCM must not be
  // classified as a shared library, even though JGit's RemoteConfig deduplicates
  // remote names as origin, origin1, origin2, ... at runtime.
  @Test
  void testIsLibrarySCMWithMultipleRemotesIsNotClassifiedAsLibrary() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      BitBucketPPRPluginConfig c = mock(BitBucketPPRPluginConfig.class);
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(c);

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      GitSCM gitScm = mock(GitSCM.class);
      RemoteConfig origin = mock(RemoteConfig.class);
      when(origin.getName()).thenReturn("origin");
      RemoteConfig origin1 = mock(RemoteConfig.class);
      when(origin1.getName()).thenReturn("origin1");
      when(gitScm.getRepositories()).thenReturn(List.of(origin, origin1));

      assertFalse(invokeIsLibrarySCM(probe, gitScm),
          "Multi-remote GitSCM must not be classified as a shared library solely from remote names (issue #378)");
    }
  }

  // Regression test for issue #380: an SCM recorded exclusively as a Pipeline
  // shared library is skipped by the probe even when the remote is named "origin".
  @Test
  void testIsRecordedOnlyAsPipelineLibraryTrueWhenActionMatchesScmAsLibraryOnly()
      throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
      action.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));

      Run<?, ?> lastBuild = mock(Run.class);
      when(lastBuild.isBuilding()).thenReturn(false);
      when(lastBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(action);

      Job<?, ?> job = mock(Job.class);
      when(job.getLastBuild()).thenAnswer(inv -> lastBuild);

      // Webhook URL via a different scheme; GitStatus.looselyMatches still matches.
      GitSCM lookupScm = mockGitScmWithUrl("https://host.example.com/org/repo.git");
      assertTrue(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, lookupScm));
    }
  }

  // Critical regression test: when the same repository was checked out BOTH as
  // a shared library AND as an explicit checkout in the same build, the probe
  // must NOT classify it as "library only" — the explicit-checkout half must
  // still react to pushes for that repo.
  @Test
  void testIsRecordedOnlyAsPipelineLibraryFalseWhenSameRepoIsAlsoExplicitlyCheckedOut()
      throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
      action.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));
      action.recordNonLibrary(uris("ssh://git@host.example.com/org/repo.git"));

      Run<?, ?> lastBuild = mock(Run.class);
      when(lastBuild.isBuilding()).thenReturn(false);
      when(lastBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(action);

      Job<?, ?> job = mock(Job.class);
      when(job.getLastBuild()).thenAnswer(inv -> lastBuild);

      GitSCM lookupScm = mockGitScmWithUrl("https://host.example.com/org/repo.git");
      assertFalse(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, lookupScm),
          "Repo present as BOTH library and explicit checkout must not be classified as library-only");
    }
  }

  // CRITICAL regression test for the tri-state scan: the most recent build that
  // has an authoritative verdict (NON_LIBRARY_OR_MIXED) must veto any older
  // build that recorded the same repository as library-only. A boolean
  // "isOnlyLibrary across the window" check would wrongly return true here.
  @Test
  void testIsRecordedOnlyAsPipelineLibraryFalseWhenLatestBuildIsMixedAndPreviousIsLibraryOnly()
      throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      // Previous build: RepoD was only loaded as a library.
      BitBucketPPRPipelineLibrarySCMAction previousAction =
          new BitBucketPPRPipelineLibrarySCMAction();
      previousAction.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));

      // Latest build: the Jenkinsfile now ALSO does an explicit checkout of RepoD.
      BitBucketPPRPipelineLibrarySCMAction latestAction =
          new BitBucketPPRPipelineLibrarySCMAction();
      latestAction.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));
      latestAction.recordNonLibrary(uris("ssh://git@host.example.com/org/repo.git"));

      Run<?, ?> previousBuild = mock(Run.class);
      when(previousBuild.isBuilding()).thenReturn(false);
      when(previousBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class))
          .thenReturn(previousAction);
      when(previousBuild.getPreviousBuild()).thenAnswer(inv -> null);

      Run<?, ?> latestBuild = mock(Run.class);
      when(latestBuild.isBuilding()).thenReturn(false);
      when(latestBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class))
          .thenReturn(latestAction);
      Run<?, ?> previousAsRaw = previousBuild;
      when(latestBuild.getPreviousBuild()).thenAnswer(inv -> previousAsRaw);

      Job<?, ?> job = mock(Job.class);
      when(job.getLastBuild()).thenAnswer(inv -> latestBuild);

      GitSCM lookupScm = mockGitScmWithUrl("https://host.example.com/org/repo.git");
      assertFalse(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, lookupScm),
          "Latest build's NON_LIBRARY_OR_MIXED verdict must veto older library-only entries");
    }
  }

  // In-progress builds reflect partial state (the library was already checked
  // out, the explicit step has not run yet). The scan must skip them and use
  // the most recent COMPLETED build's verdict instead.
  @Test
  void testIsRecordedOnlyAsPipelineLibrarySkipsInProgressBuilds() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      // In-progress build: library recorded, explicit not yet (partial state).
      BitBucketPPRPipelineLibrarySCMAction partial = new BitBucketPPRPipelineLibrarySCMAction();
      partial.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));

      // Completed earlier build: full mixed verdict.
      BitBucketPPRPipelineLibrarySCMAction completed = new BitBucketPPRPipelineLibrarySCMAction();
      completed.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));
      completed.recordNonLibrary(uris("ssh://git@host.example.com/org/repo.git"));

      Run<?, ?> earlierBuild = mock(Run.class);
      when(earlierBuild.isBuilding()).thenReturn(false);
      when(earlierBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(completed);
      when(earlierBuild.getPreviousBuild()).thenAnswer(inv -> null);

      Run<?, ?> inProgress = mock(Run.class);
      when(inProgress.isBuilding()).thenReturn(true);
      when(inProgress.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(partial);
      Run<?, ?> earlierAsRaw = earlierBuild;
      when(inProgress.getPreviousBuild()).thenAnswer(inv -> earlierAsRaw);

      Job<?, ?> job = mock(Job.class);
      when(job.getLastBuild()).thenAnswer(inv -> inProgress);

      GitSCM lookupScm = mockGitScmWithUrl("https://host.example.com/org/repo.git");
      assertFalse(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, lookupScm),
          "Probe must skip in-progress build's partial verdict and honour the completed one");
    }
  }

  @Test
  void testIsRecordedOnlyAsPipelineLibraryFalseWhenNoLastBuild() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();
      Job<?, ?> job = mock(Job.class);
      when(job.getLastBuild()).thenAnswer(inv -> null);

      GitSCM scm = mockGitScmWithUrl("ssh://git@host.example.com/org/repo.git");
      assertFalse(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, scm));
    }
  }

  // The probe scans a window of recent builds, not only lastBuild — if the
  // most recent build had no action (e.g. pre-upgrade build) but a previous
  // build did, the filter still applies.
  @Test
  void testIsRecordedOnlyAsPipelineLibraryScansPreviousBuildsWhenLastBuildHasNoAction()
      throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
      action.recordLibrary(uris("ssh://git@host.example.com/org/lib.git"));

      Run<?, ?> earlierBuild = mock(Run.class);
      when(earlierBuild.isBuilding()).thenReturn(false);
      when(earlierBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(action);
      when(earlierBuild.getPreviousBuild()).thenAnswer(inv -> null);

      Run<?, ?> lastBuild = mock(Run.class);
      when(lastBuild.isBuilding()).thenReturn(false);
      when(lastBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(null);
      Run<?, ?> earlierAsRaw = earlierBuild;
      when(lastBuild.getPreviousBuild()).thenAnswer(inv -> earlierAsRaw);

      Job<?, ?> job = mock(Job.class);
      when(job.getLastBuild()).thenAnswer(inv -> lastBuild);

      GitSCM lookupScm = mockGitScmWithUrl("https://host.example.com/org/lib.git");
      assertTrue(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, lookupScm),
          "Multi-build scan should find a recorded library on a previous build");
    }
  }

  // Operational cap: a long chain of null-action builds (e.g. a job upgraded
  // with thousands of pre-listener builds in its history) must not let the
  // scan walk indefinitely. After MAX_BUILDS_TO_SCAN visits the scan stops
  // even if an authoritative verdict is sitting just past the cap.
  @Test
  void testIsRecordedOnlyAsPipelineLibraryStopsAtMaxBuildsToScan() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      // The 51st build (just past the operational cap of 50) carries the
      // ONLY_LIBRARY verdict, but the scan must never reach it.
      BitBucketPPRPipelineLibrarySCMAction libraryAction =
          new BitBucketPPRPipelineLibrarySCMAction();
      libraryAction.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));

      Run<?, ?> beyondCap = mock(Run.class);
      when(beyondCap.isBuilding()).thenReturn(false);
      when(beyondCap.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(libraryAction);
      when(beyondCap.getPreviousBuild()).thenAnswer(inv -> null);

      // Chain 50 null-action pre-upgrade builds in front of it.
      Run<?, ?> chain = beyondCap;
      for (int i = 0; i < 50; i++) {
        Run<?, ?> step = mock(Run.class);
        when(step.isBuilding()).thenReturn(false);
        when(step.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(null);
        Run<?, ?> next = chain;
        when(step.getPreviousBuild()).thenAnswer(inv -> next);
        chain = step;
      }

      Job<?, ?> job = mock(Job.class);
      Run<?, ?> head = chain;
      when(job.getLastBuild()).thenAnswer(inv -> head);

      GitSCM lookupScm = mockGitScmWithUrl("https://host.example.com/org/repo.git");
      assertFalse(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, lookupScm),
          "Scan must stop at MAX_BUILDS_TO_SCAN and not walk thousands of pre-upgrade builds");
    }
  }

  // Once no recent build records the library, the protection lapses (the
  // documented limitation of any window-based scan).
  @Test
  void testIsRecordedOnlyAsPipelineLibraryFalseWhenWindowHasNoMatch() throws Exception {
    try (MockedStatic<BitBucketPPRPluginConfig> config =
        Mockito.mockStatic(BitBucketPPRPluginConfig.class)) {
      config.when(BitBucketPPRPluginConfig::getInstance).thenReturn(mock(BitBucketPPRPluginConfig.class));

      BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

      Run<?, ?> lastBuild = mock(Run.class);
      when(lastBuild.isBuilding()).thenReturn(false);
      when(lastBuild.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenReturn(null);
      when(lastBuild.getPreviousBuild()).thenAnswer(inv -> null);

      Job<?, ?> job = mock(Job.class);
      when(job.getLastBuild()).thenAnswer(inv -> lastBuild);

      GitSCM scm = mockGitScmWithUrl("ssh://git@host.example.com/org/repo.git");
      assertFalse(invokeIsRecordedOnlyAsPipelineLibrary(probe, job, scm));
    }
  }

  private boolean invokeMPJobShouldNotBeTriggered(BitBucketPPRJobProbe probe, Job<?, ?> job,
      BitBucketPPRHookEvent event, BitBucketPPRAction action) throws Exception {
    Method method = BitBucketPPRJobProbe.class.getDeclaredMethod("mPJobShouldNotBeTriggered",
        Job.class, BitBucketPPRHookEvent.class, BitBucketPPRAction.class);
    method.setAccessible(true);
    return (boolean) method.invoke(probe, job, event, action);
  }

  private static BitBucketPPRHookEvent prEvent() {
    BitBucketPPRHookEvent event = mock(BitBucketPPRHookEvent.class);
    when(event.getAction()).thenReturn("pullrequest:comment_created");
    return event;
  }

  private static BitBucketPPRAction prAction(String sourceBranch, String targetBranch) {
    BitBucketPPRAction action = mock(BitBucketPPRAction.class);
    when(action.getSourceBranch()).thenReturn(sourceBranch);
    when(action.getTargetBranch()).thenReturn(targetBranch);
    return action;
  }

  // Mirrors the real PullRequestSCMHead, which both extends SCMHead and
  // implements ChangeRequestSCMHead2; getOriginName() carries the source branch.
  private static SCMHead mockPrHead(String originBranch) {
    SCMHead head = mock(SCMHead.class, Mockito.withSettings().extraInterfaces(ChangeRequestSCMHead2.class));
    when(((ChangeRequestSCMHead2) head).getOriginName()).thenReturn(originBranch);
    return head;
  }

  // Regression test for issue #388: bitbucket-branch-source sets a multibranch PR
  // job's display name to the PR *title* (e.g. "release(demo-api): - release/next
  // (#11)"), so the old displayName-vs-sourceBranch comparison always failed and
  // the job was skipped. The match must instead use the PR head's origin branch.
  //
  // Note on the chosen API: for a PullRequestSCMHead, getName() is "PR-<id>" (e.g.
  // "PR-11") — NOT the branch — so getName() would still mismatch "release/next".
  // getOriginName() (from ChangeRequestSCMHead2) is the source branch, hence used.
  @Test
  void testMpJobTriggeredForPrJobWhenDisplayNameIsPrTitle() throws Exception {
    BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

    Job<?, ?> job = mock(Job.class);
    when(job.getDisplayName()).thenReturn("release(demo-api): - release/next (#11)");

    SCMHead prHead = mockPrHead("release/next");

    try (MockedStatic<SCMHead.HeadByItem> heads = Mockito.mockStatic(SCMHead.HeadByItem.class)) {
      heads.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(prHead);

      assertFalse(
          invokeMPJobShouldNotBeTriggered(probe, job, prEvent(), prAction("release/next", "main")),
          "PR job must be triggered when the PR origin branch matches, despite a PR-title display name (#388)");
    }
  }

  // The PR-head match must still discriminate: an event for a different source
  // branch must not trigger this PR's job.
  @Test
  void testMpJobNotTriggeredForPrJobOnDifferentSourceBranch() throws Exception {
    BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

    Job<?, ?> job = mock(Job.class);
    when(job.getDisplayName()).thenReturn("release(demo-api): - release/next (#11)");

    SCMHead prHead = mockPrHead("release/next");

    try (MockedStatic<SCMHead.HeadByItem> heads = Mockito.mockStatic(SCMHead.HeadByItem.class)) {
      heads.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(prHead);

      assertTrue(
          invokeMPJobShouldNotBeTriggered(probe, job, prEvent(), prAction("feature/unrelated", "main")),
          "PR job must be skipped when the event's source branch is not this PR's origin branch");
    }
  }

  // A plain (non-PR) multibranch branch job is matched on the head name, which is
  // the branch name. This must keep working independently of the display name.
  @Test
  void testMpJobTriggeredForBranchJobMatchingByHeadName() throws Exception {
    BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

    Job<?, ?> job = mock(Job.class);
    when(job.getDisplayName()).thenReturn("a custom display name");

    SCMHead branchHead = new SCMHead("feature/foo");

    try (MockedStatic<SCMHead.HeadByItem> heads = Mockito.mockStatic(SCMHead.HeadByItem.class)) {
      heads.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(branchHead);

      assertFalse(
          invokeMPJobShouldNotBeTriggered(probe, job, prEvent(), prAction("feature/foo", "main")),
          "Branch job must be triggered when the head (branch) name matches the source branch");
    }
  }

  // Freestyle / standalone jobs have no SCMHead: the probe must fall back to the
  // original displayName comparison, preserving pre-#388 behavior for those jobs.
  @Test
  void testMpJobFallsBackToDisplayNameWhenNoScmHead() throws Exception {
    BitBucketPPRJobProbe probe = new BitBucketPPRJobProbe();

    Job<?, ?> job = mock(Job.class);
    when(job.getDisplayName()).thenReturn("feature/foo");

    try (MockedStatic<SCMHead.HeadByItem> heads = Mockito.mockStatic(SCMHead.HeadByItem.class)) {
      heads.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(null);

      assertFalse(
          invokeMPJobShouldNotBeTriggered(probe, job, prEvent(), prAction("feature/foo", "main")),
          "Without an SCMHead the probe must fall back to the display-name comparison (trigger on match)");
      assertTrue(
          invokeMPJobShouldNotBeTriggered(probe, job, prEvent(), prAction("other-branch", "main")),
          "Without an SCMHead a non-matching display name must still skip the job");
    }
  }

}
