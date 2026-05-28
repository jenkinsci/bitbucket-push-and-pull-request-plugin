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

import static io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction.Classification.NON_LIBRARY_OR_MIXED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction.Classification.ONLY_LIBRARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitBucketPPRSCMCheckoutListenerTest {

  @Test
  void isPipelineLibraryWorkspaceTrueForLegacyLibsSuffix() {
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA@libs/abc123"));
    assertTrue(BitBucketPPRSCMCheckoutListener.isPipelineLibraryWorkspace(workspace));
  }

  // The bare `@libs` suffix (no trailing segment) is intentionally NOT matched:
  // it collides with user-chosen job names ending in `@libs`. The legacy
  // retriever always appends a library directory, so `@libs` followed by a path
  // separator and at least one character is the authoritative shape.
  @Test
  void isPipelineLibraryWorkspaceFalseForBareLibsSuffix() {
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA@libs"));
    assertFalse(BitBucketPPRSCMCheckoutListener.isPipelineLibraryWorkspace(workspace));
  }

  @Test
  void isPipelineLibraryWorkspaceFalseForRegularWorkspace() {
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA/ready_for_review"));
    assertFalse(BitBucketPPRSCMCheckoutListener.isPipelineLibraryWorkspace(workspace));
  }

  @Test
  void isPipelineLibraryWorkspaceFalseForLookalikePathSegment() {
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA@libstuff/abc"));
    assertFalse(BitBucketPPRSCMCheckoutListener.isPipelineLibraryWorkspace(workspace));
  }

  @Test
  void isPipelineLibraryWorkspaceFalseForNull() {
    assertFalse(BitBucketPPRSCMCheckoutListener.isPipelineLibraryWorkspace(null));
  }

  @Test
  void onCheckoutRecordsLibraryWhenWorkspaceMatchesAndJobHasTrigger() throws URISyntaxException {
    WorkflowRun run = newRunForJobWithTrigger(true);

    GitSCM scm = singleRemoteGitScm("ssh://git@host/org/repo.git");
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA@libs/abc"));

    new BitBucketPPRSCMCheckoutListener()
        .onCheckout(run, scm, workspace, mock(TaskListener.class), null, null);

    BitBucketPPRPipelineLibrarySCMAction action =
        run.getAction(BitBucketPPRPipelineLibrarySCMAction.class);
    assertNotNull(action);
    assertEquals(ONLY_LIBRARY, action.classify(scm),
        "Library workspace checkout must be recorded as a library");
  }

  // Non-library checkouts must be recorded too — without them the action would
  // be unable to tell apart "RepoD is only a library" from "RepoD is a library
  // and also an explicit checkout".
  @Test
  void onCheckoutRecordsNonLibraryWhenWorkspaceDoesNotMatch() throws URISyntaxException {
    WorkflowRun run = newRunForJobWithTrigger(true);

    GitSCM scm = singleRemoteGitScm("ssh://git@host/org/repo.git");
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA/ready_for_review"));

    new BitBucketPPRSCMCheckoutListener()
        .onCheckout(run, scm, workspace, mock(TaskListener.class), null, null);

    BitBucketPPRPipelineLibrarySCMAction action =
        run.getAction(BitBucketPPRPipelineLibrarySCMAction.class);
    assertNotNull(action,
        "Action must be installed so that future library-only matches can be vetoed");
    assertEquals(NON_LIBRARY_OR_MIXED, action.classify(scm),
        "An SCM seen only as a non-library checkout must classify as non-library");
  }

  // Critical regression test: a job that uses RepoD BOTH as a Pipeline shared
  // library AND as an explicit checkout in the same build must NOT be excluded
  // from webhook matching for RepoD — the explicit-checkout half still needs to
  // react to pushes.
  @Test
  void onCheckoutMarksAsNonLibraryWhenSameRepoIsAlsoExplicitlyCheckedOut()
      throws URISyntaxException {
    WorkflowRun run = newRunForJobWithTrigger(true);

    GitSCM librarySide = singleRemoteGitScm("ssh://git@host/org/repo.git");
    GitSCM explicitSide = singleRemoteGitScm("https://host/org/repo.git");
    FilePath libWorkspace = new FilePath(new File("/jenkins/workspace/RepoA@libs/abc"));
    FilePath explicitWorkspace = new FilePath(new File("/jenkins/workspace/RepoA/sub"));

    BitBucketPPRSCMCheckoutListener listener = new BitBucketPPRSCMCheckoutListener();
    listener.onCheckout(run, librarySide, libWorkspace, mock(TaskListener.class), null, null);
    listener.onCheckout(run, explicitSide, explicitWorkspace, mock(TaskListener.class), null, null);

    BitBucketPPRPipelineLibrarySCMAction action =
        run.getAction(BitBucketPPRPipelineLibrarySCMAction.class);
    assertNotNull(action);
    assertEquals(NON_LIBRARY_OR_MIXED, action.classify(librarySide),
        "Repo present as BOTH library AND explicit checkout must classify as non-library");
    assertEquals(NON_LIBRARY_OR_MIXED, action.classify(explicitSide),
        "Repo present as BOTH library AND explicit checkout must classify as non-library");
  }

  // Scoping: listener must NOT add the plugin's Action to builds of jobs that
  // do not use this plugin's trigger, so uninstalling the plugin does not orphan
  // io.jenkins.plugins.bitbucketpushandpullrequest classes in unrelated builds.
  @Test
  void onCheckoutDoesNothingForJobsWithoutBitBucketTrigger() throws URISyntaxException {
    WorkflowRun run = newRunForJobWithTrigger(false);
    GitSCM scm = singleRemoteGitScm("ssh://git@host/org/repo.git");
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA@libs/abc"));

    new BitBucketPPRSCMCheckoutListener()
        .onCheckout(run, scm, workspace, mock(TaskListener.class), null, null);

    verify(run, never()).addAction(any(Action.class));
  }

  // Idempotency: a second onCheckout call on the same Run must reuse the action
  // it just installed rather than calling addAction twice.
  @Test
  void onCheckoutInstallsActionExactlyOnceAcrossMultipleCheckouts() throws URISyntaxException {
    WorkflowRun run = newRunForJobWithTrigger(true);
    GitSCM libA = singleRemoteGitScm("ssh://git@host/org/libA.git");
    GitSCM libB = singleRemoteGitScm("ssh://git@host/org/libB.git");
    FilePath wsA = new FilePath(new File("/jenkins/workspace/RepoA@libs/libA"));
    FilePath wsB = new FilePath(new File("/jenkins/workspace/RepoA@libs/libB"));

    BitBucketPPRSCMCheckoutListener listener = new BitBucketPPRSCMCheckoutListener();
    listener.onCheckout(run, libA, wsA, mock(TaskListener.class), null, null);
    listener.onCheckout(run, libB, wsB, mock(TaskListener.class), null, null);

    verify(run, times(1)).addAction(any(BitBucketPPRPipelineLibrarySCMAction.class));
    BitBucketPPRPipelineLibrarySCMAction action =
        run.getAction(BitBucketPPRPipelineLibrarySCMAction.class);
    assertEquals(ONLY_LIBRARY, action.classify(libA));
    assertEquals(ONLY_LIBRARY, action.classify(libB));
  }

  // Defensive: a RuntimeException inside the URI extraction must never propagate
  // out of onCheckout, and must NOT leave an empty action on the Run.
  @Test
  void onCheckoutSwallowsRuntimeExceptionsAndDoesNotAttachEmptyAction() {
    WorkflowRun run = newRunForJobWithTrigger(true);
    GitSCM brokenScm = mock(GitSCM.class);
    when(brokenScm.getRepositories()).thenThrow(new RuntimeException("malformed config"));
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA@libs/abc"));

    new BitBucketPPRSCMCheckoutListener()
        .onCheckout(run, brokenScm, workspace, mock(TaskListener.class), null, null);

    verify(run, never()).addAction(any(Action.class));
  }

  @Test
  void onCheckoutIgnoresNonGitScm() {
    WorkflowRun run = newRunForJobWithTrigger(true);
    SCM nonGit = mock(SCM.class);
    FilePath workspace = new FilePath(new File("/jenkins/workspace/RepoA@libs/abc"));

    new BitBucketPPRSCMCheckoutListener()
        .onCheckout(run, nonGit, workspace, mock(TaskListener.class), null, null);

    verify(run, never()).addAction(any(Action.class));
  }

  private WorkflowRun newRunForJobWithTrigger(boolean hasTrigger) {
    WorkflowRun run = mock(WorkflowRun.class);
    WorkflowJob job = mock(WorkflowJob.class);
    when(run.getParent()).thenReturn(job);

    if (hasTrigger) {
      BitBucketPPRTrigger trigger = mock(BitBucketPPRTrigger.class);
      Map<?, ? extends Trigger<?>> triggers =
          Map.of(mock(hudson.triggers.TriggerDescriptor.class), trigger);
      @SuppressWarnings({"unchecked", "rawtypes"})
      Map<hudson.triggers.TriggerDescriptor, hudson.triggers.Trigger<?>> erased =
          (Map) triggers;
      when(job.getTriggers()).thenReturn(erased);
    } else {
      when(job.getTriggers()).thenReturn(Map.of());
    }

    List<Action> actions = new ArrayList<>();
    when(run.getAction(BitBucketPPRPipelineLibrarySCMAction.class)).thenAnswer(inv ->
        actions.stream()
            .filter(BitBucketPPRPipelineLibrarySCMAction.class::isInstance)
            .map(BitBucketPPRPipelineLibrarySCMAction.class::cast)
            .findFirst().orElse(null));
    doAnswer(inv -> {
      actions.add(inv.getArgument(0));
      return null;
    }).when(run).addAction(any(Action.class));
    when(run.getFullDisplayName()).thenReturn("RepoA #1");
    return run;
  }

  private GitSCM singleRemoteGitScm(String url) throws URISyntaxException {
    GitSCM scm = mock(GitSCM.class);
    RemoteConfig remote = mock(RemoteConfig.class);
    when(remote.getURIs()).thenReturn(List.of(new URIish(url)));
    when(scm.getRepositories()).thenReturn(List.of(remote));
    return scm;
  }
}
