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

import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import java.lang.reflect.Method;
import java.util.List;
import org.eclipse.jgit.transport.RemoteConfig;
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

}
