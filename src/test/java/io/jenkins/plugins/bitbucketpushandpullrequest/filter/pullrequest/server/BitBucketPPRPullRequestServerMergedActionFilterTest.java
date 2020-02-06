package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.EnvVars;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRPullRequestServerMergedActionFilterTest {

  @Test
  public void testMatches() {
    String allowedBranches = "master";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

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

    allowedBranches = ("*/my.branch/*");

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
  public void testMatchEnv() {
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

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

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
  public void testUsesRefsHeads() {
    String allowedBranches = "refs/heads/j*n*";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);


    assertTrue(c.matches(allowedBranches, "refs/heads/jenkins", null));
    assertTrue(c.matches(allowedBranches, "refs/heads/jane", null));
    assertTrue(c.matches(allowedBranches, "refs/heads/jones", null));
    assertFalse(c.matches(allowedBranches, "origin/jenkins", null));
    assertFalse(c.matches(allowedBranches, "remote/origin/jane", null));
  }

  @Test
  public void testUsesJavaPatternDirectlyIfPrefixedWithColon() {

    String allowedBranches = ":^(?!(origin/prefix)).*";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin", null));
    assertTrue(c.matches(allowedBranches, "origin/master", null));
    assertTrue(c.matches(allowedBranches, "origin/feature", null));
    assertFalse(c.matches(allowedBranches, "origin/prefix_123", null));
    assertFalse(c.matches(allowedBranches, "origin/prefix", null));
    assertFalse(c.matches(allowedBranches, "origin/prefix-abc", null));
  }

  @Test
  public void testMatchesNot1() {
    String allowedBranches = "*/master";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

    assertFalse(c.matches(allowedBranches, "master", null));
  }

  @Test
  public void testMatchesNot2() {
    String allowedBranches = "develop, :^(?!master$).*";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

    assertFalse(c.matches(allowedBranches, "master", null));

    allowedBranches = ":^(?!develop$).*";
    assertFalse(c.matches(allowedBranches, "develop", null));
  }

  @Test
  public void testMatchesEmptyBranches() {

    String allowedBranches = "";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

    assertTrue(c.matches(allowedBranches, "master", null));
    assertTrue(c.matches(allowedBranches, "develop", null));
    assertTrue(c.matches(allowedBranches, "feature/new-stuff", null));
  }


  @Test
  public void testUsesJavaPatternWithRepetition() {
    String allowedBranches = ":origin/release-\\d{8}";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/release-20150101", null));
    assertFalse(c.matches(allowedBranches, "origin/release-2015010", null));
    assertFalse(c.matches(allowedBranches, "origin/release-201501011", null));
    assertFalse(c.matches(allowedBranches, "origin/release-20150101-something", null));
  }

  @Test
  public void testUsesJavaPatternToExcludeMultipleBranches() {
    String allowedBranches = ":^(?!origin/master$|origin/develop$).*";

    BitBucketPPRPullRequestServerMergedActionFilter c =
        new BitBucketPPRPullRequestServerMergedActionFilter(allowedBranches);

    assertTrue(c.matches(allowedBranches, "origin/branch1", null));
    assertTrue(c.matches(allowedBranches, "origin/branch-2", null));
    assertTrue(c.matches(allowedBranches, "origin/master123", null));
    assertTrue(c.matches(allowedBranches, "origin/develop-123", null));
    assertFalse(c.matches(allowedBranches, "origin/master", null));
    assertFalse(c.matches(allowedBranches, "origin/develop", null));
  }
}
