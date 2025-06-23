package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.EnvVars;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class BitBucketPPRServerRepositoryPushActionFilterTest {

  @Test
  void testMatches() {
    String allowedBranches = "master";

    BitBucketPPRServerRepositoryPushActionFilter c =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/master", null));
    assertFalse(c.matches(allowedBranches, "origin/something/master", null));
    assertTrue(c.matches(allowedBranches, "master", null));
    assertFalse(c.matches(allowedBranches, "dev", null));

    allowedBranches = "origin/*/dev";

    assertFalse(c.matches(allowedBranches, "origintestdev", null));
    assertTrue(c.matches(allowedBranches, "origin/test/dev", null));
    assertFalse(c.matches(allowedBranches, "origin/test/release", null));
    assertFalse(c.matches(allowedBranches, "origin/test/something/release", null));

    allowedBranches = "origin/*";
    assertTrue(c.matches(allowedBranches, "origin/master", null));

    allowedBranches = "**/magnayn/*";
    assertTrue(c.matches(allowedBranches, "origin/magnayn/b1", null));
    assertTrue(c.matches(allowedBranches, "remote/origin/magnayn/b1", null));
    assertTrue(c.matches(allowedBranches, "remotes/origin/magnayn/b1", null));

    allowedBranches = "*/my.branch/*";
    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", null));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", null));
    assertFalse(c.matches(allowedBranches, "remote/origin/my.branch/b1", null));
    assertTrue(c.matches(allowedBranches, "remotes/origin/my.branch/b1", null));

    allowedBranches = "**";
    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", null));
    assertTrue(c.matches(allowedBranches, "origin/my-branch/b1", null));
    assertTrue(c.matches(allowedBranches, "remote/origin/my.branch/b1", null));
    assertTrue(c.matches(allowedBranches, "remotes/origin/my.branch/b1", null));

    allowedBranches = "*";
    assertTrue(c.matches(allowedBranches, "origin/x", null));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", null));
  }

  @Test
  void testMatchEnv() {
    HashMap<String, String> envMap = new HashMap<>();
    envMap.put("master", "master");
    envMap.put("origin", "origin");
    envMap.put("dev", "dev");
    envMap.put("magnayn", "magnayn");
    envMap.put("mybranch", "my.branch");
    envMap.put("anyLong", "**");
    envMap.put("anyShort", "*");
    envMap.put("anyEmpty", "");
    EnvVars env = new EnvVars(envMap);

    String allowedBranches = "${master}";

    BitBucketPPRServerRepositoryPushActionFilter c =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/master", env));
    assertFalse(c.matches(allowedBranches, "origin/something/master", env));
    assertTrue(c.matches(allowedBranches, "master", env));
    assertFalse(c.matches(allowedBranches, "dev", env));

    allowedBranches = "${origin}/*/${dev}";
    assertFalse(c.matches(allowedBranches, "origintestdev", env));
    assertTrue(c.matches(allowedBranches, "origin/test/dev", env));
    assertFalse(c.matches(allowedBranches, "origin/test/release", env));
    assertFalse(c.matches(allowedBranches, "origin/test/something/release", env));

    allowedBranches = "${origin}/*";
    assertTrue(c.matches(allowedBranches, "origin/master", env));

    allowedBranches = "**/${magnayn}/*";
    assertTrue(c.matches(allowedBranches, "origin/magnayn/b1", env));
    assertTrue(c.matches(allowedBranches, "remote/origin/magnayn/b1", env));

    allowedBranches = "*/${mybranch}/*";
    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", env));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", env));
    assertFalse(c.matches(allowedBranches, "remote/origin/my.branch/b1", env));

    allowedBranches = "${anyLong}";

    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", env));
    assertTrue(c.matches(allowedBranches, "origin/my-branch/b1", env));
    assertTrue(c.matches(allowedBranches, "remote/origin/my.branch/b1", env));

    allowedBranches = "${anyShort}";
    assertTrue(c.matches(allowedBranches, "origin/x", env));
    assertFalse(c.matches(allowedBranches, "origin/my-branch/b1", env));

    allowedBranches = "${anyEmpty}";
    assertTrue(c.matches(allowedBranches, "origin/my.branch/b1", env));
    assertTrue(c.matches(allowedBranches, "origin/my-branch/b1", env));
    assertTrue(c.matches(allowedBranches, "remote/origin/my.branch/b1", env));
  }

  @Test
  void testUsesRefsHeads() {
    String allowedBranches = "refs/heads/j*n*";

    BitBucketPPRServerRepositoryPushActionFilter c =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);

    assertTrue(c.matches(allowedBranches, "refs/heads/jenkins", null));
    assertTrue(c.matches(allowedBranches, "refs/heads/jane", null));
    assertTrue(c.matches(allowedBranches, "refs/heads/jones", null));
    assertFalse(c.matches(allowedBranches, "origin/jenkins", null));
    assertFalse(c.matches(allowedBranches, "remote/origin/jane", null));
  }

  @Test
  void testUsesJavaPatternDirectlyIfPrefixedWithColon() {
    String allowedBranches = ":^(?!(origin/prefix)).*";

    BitBucketPPRServerRepositoryPushActionFilter m =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);

    assertTrue(m.matches(allowedBranches, "origin", null));
    assertTrue(m.matches(allowedBranches, "origin/master", null));
    assertTrue(m.matches(allowedBranches, "origin/feature", null));
    assertFalse(m.matches(allowedBranches, "origin/prefix_123", null));
    assertFalse(m.matches(allowedBranches, "origin/prefix", null));
    assertFalse(m.matches(allowedBranches, "origin/prefix-abc", null));
  }

  @Test
  void testMatchesNot1() {
    String allowedBranches = "*/master";

    BitBucketPPRServerRepositoryPushActionFilter c =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);

    assertFalse(c.matches(allowedBranches, "master", null));
  }

  @Test
  void testMatchesNot2() {
    String allowedBranches = "develop, :^(?!master$).*";

    BitBucketPPRServerRepositoryPushActionFilter c =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);
    assertFalse(c.matches(allowedBranches, "master", null));

    allowedBranches = ":^(?!develop$).*";
    assertFalse(c.matches(allowedBranches, "develop", null));
  }

  @Test
  void testMatchesEmptyBranches() {
    String allowedBranches = "";
    BitBucketPPRServerRepositoryPushActionFilter c =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);

    assertTrue(c.matches(allowedBranches, "master", null));
    assertTrue(c.matches(allowedBranches, "develop", null));
    assertTrue(c.matches(allowedBranches, "feature/new-stuff", null));
  }

  @Test
  void testUsesJavaPatternWithRepetition() {
    String allowedBranches = ":origin/release-\\d{8}";

    BitBucketPPRServerRepositoryPushActionFilter m =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);
    assertTrue(m.matches(allowedBranches, "origin/release-20150101", null));
    assertFalse(m.matches(allowedBranches, "origin/release-2015010", null));
    assertFalse(m.matches(allowedBranches, "origin/release-201501011", null));
    assertFalse(m.matches(allowedBranches, "origin/release-20150101-something", null));
  }

  @Test
  void testUsesJavaPatternToExcludeMultipleBranches() {
    String allowedBranches = ":^(?!origin/master$|origin/develop$).*";

    BitBucketPPRServerRepositoryPushActionFilter m =
        new BitBucketPPRServerRepositoryPushActionFilter(false, false, allowedBranches);

    assertTrue(m.matches(allowedBranches, "origin/branch1", null));
    assertTrue(m.matches(allowedBranches, "origin/branch-2", null));
    assertTrue(m.matches(allowedBranches, "origin/master123", null));
    assertTrue(m.matches(allowedBranches, "origin/develop-123", null));
    assertFalse(m.matches(allowedBranches, "origin/master", null));
    assertFalse(m.matches(allowedBranches, "origin/develop", null));
  }

  @Test
  void shouldTriggerBuildReturnsFalseIsTypeNotSet() {
    BitBucketPPRAction bitbucketAction = Mockito.mock(BitBucketPPRAction.class);
    Mockito.when(bitbucketAction.getType()).thenReturn(null);
    BitBucketPPRServerRepositoryPushActionFilter c = new BitBucketPPRServerRepositoryPushActionFilter(
        false, false, "master");
    assertFalse(c.shouldTriggerBuild(bitbucketAction));
  }
}
