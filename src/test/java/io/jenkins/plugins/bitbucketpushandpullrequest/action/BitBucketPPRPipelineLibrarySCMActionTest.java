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

import static io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction.Classification.NON_LIBRARY_OR_MIXED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction.Classification.ONLY_LIBRARY;
import static io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRPipelineLibrarySCMAction.Classification.UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.plugins.git.GitSCM;
import java.net.URISyntaxException;
import java.util.List;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.Test;

class BitBucketPPRPipelineLibrarySCMActionTest {

  @Test
  void classifyOnlyLibraryAcrossUrlSchemes() throws URISyntaxException {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));

    GitSCM lookup = singleRemoteScm("https://host.example.com/org/repo.git");
    assertEquals(ONLY_LIBRARY, action.classify(lookup),
        "Equivalent URLs in different schemes should match via GitStatus.looselyMatches");
  }

  // Host-case behaviour is delegated to GitStatus.looselyMatches, which today
  // compares hosts case-sensitively (Objects.equals). The action mirrors that
  // behaviour deliberately so the negative-match path stays in lockstep with
  // the probe's positive matchGitScm path.
  @Test
  void classifyIsHostCaseSensitiveLikeGitStatusLooselyMatches() throws URISyntaxException {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(uris("ssh://git@Bitbucket.Example.COM/org/repo.git"));

    GitSCM lookup = singleRemoteScm("https://bitbucket.example.com/org/repo.git");
    assertEquals(UNKNOWN, action.classify(lookup),
        "Host case mismatch must not match — symmetric with GitStatus.looselyMatches");
  }

  @Test
  void classifyUnknownForUnrelatedRepository() throws URISyntaxException {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));

    GitSCM other = singleRemoteScm("ssh://git@host.example.com/org/other.git");
    assertEquals(UNKNOWN, action.classify(other));
  }

  // Multi-remote SCM is by construction a real source SCM, never a library
  // (#378). The classification returns NON_LIBRARY_OR_MIXED rather than UNKNOWN
  // so the probe's scan stops here instead of regressing to an older build's
  // ONLY_LIBRARY verdict for a coincidentally-matching URI.
  @Test
  void classifyMultiRemoteAsNonLibraryOrMixed() throws URISyntaxException {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(uris("ssh://git@host.example.com/org/lib.git"));

    GitSCM multiRemote = mock(GitSCM.class);
    RemoteConfig primary = mock(RemoteConfig.class);
    when(primary.getURIs()).thenReturn(List.of(new URIish("ssh://git@host.example.com/org/primary.git")));
    RemoteConfig mirror = mock(RemoteConfig.class);
    when(mirror.getURIs()).thenReturn(List.of(new URIish("ssh://git@host.example.com/org/lib.git")));
    when(multiRemote.getRepositories()).thenReturn(List.of(primary, mirror));

    assertEquals(NON_LIBRARY_OR_MIXED, action.classify(multiRemote),
        "Multi-remote GitSCM must classify as NON_LIBRARY_OR_MIXED (preserves #378)");
  }

  // Same URL recorded as BOTH library AND explicit checkout in this build:
  // the explicit-checkout half must keep triggering on pushes, so the
  // classification must be NON_LIBRARY_OR_MIXED.
  @Test
  void classifyAsNonLibraryWhenSameRepoIsAlsoExplicit() throws URISyntaxException {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));
    action.recordNonLibrary(uris("ssh://git@host.example.com/org/repo.git"));

    GitSCM lookup = singleRemoteScm("https://host.example.com/org/repo.git");
    assertEquals(NON_LIBRARY_OR_MIXED, action.classify(lookup));
  }

  @Test
  void classifyDistinguishesLibraryFromExplicitInMixedBuild() throws URISyntaxException {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(uris("ssh://git@host.example.com/org/lib.git"));
    action.recordNonLibrary(uris("ssh://git@host.example.com/org/source.git"));

    assertEquals(ONLY_LIBRARY, action.classify(singleRemoteScm("https://host.example.com/org/lib.git")));
    assertEquals(NON_LIBRARY_OR_MIXED,
        action.classify(singleRemoteScm("https://host.example.com/org/source.git")));
    assertEquals(UNKNOWN,
        action.classify(singleRemoteScm("https://host.example.com/org/unrelated.git")));
  }

  @Test
  void recordIsIdempotentForLooselyEquivalentUris() throws URISyntaxException {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(uris("ssh://git@host.example.com/org/repo.git"));
    action.recordLibrary(uris("https://host.example.com/org/repo.git"));
    action.recordLibrary(uris("git@host.example.com:org/repo.git"));

    action.recordLibrary(uris("ssh://git@host.example.com/org/other.git"));
    assertEquals(ONLY_LIBRARY,
        action.classify(singleRemoteScm("ssh://git@host.example.com/org/repo.git")));
    assertEquals(ONLY_LIBRARY,
        action.classify(singleRemoteScm("ssh://git@host.example.com/org/other.git")));
    assertEquals(UNKNOWN,
        action.classify(singleRemoteScm("ssh://git@host.example.com/org/third.git")));
  }

  @Test
  void classifyHandlesNullScm() {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    assertEquals(UNKNOWN, action.classify(null));
  }

  @Test
  void recordHandlesNullUris() {
    BitBucketPPRPipelineLibrarySCMAction action = new BitBucketPPRPipelineLibrarySCMAction();
    action.recordLibrary(null);
    action.recordNonLibrary(null);
    // No throw; classification of an unrelated repo stays UNKNOWN.
  }

  private static List<URIish> uris(String url) throws URISyntaxException {
    return List.of(new URIish(url));
  }

  private GitSCM singleRemoteScm(String url) throws URISyntaxException {
    GitSCM scm = mock(GitSCM.class);
    RemoteConfig remote = mock(RemoteConfig.class);
    when(remote.getURIs()).thenReturn(List.of(new URIish(url)));
    when(scm.getRepositories()).thenReturn(List.of(remote));
    return scm;
  }
}
