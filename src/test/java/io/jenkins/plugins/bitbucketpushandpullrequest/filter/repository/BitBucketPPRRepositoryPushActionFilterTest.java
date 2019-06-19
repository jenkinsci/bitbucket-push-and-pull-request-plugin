package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import hudson.EnvVars;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRRepositoryPushActionFilterTest {

  @Test
  public void testMatches() {
    BitBucketPPRRepositoryPushActionFilter c =
        new BitBucketPPRRepositoryPushActionFilter(false, "master");

    assertTrue(c.matches("origin/master"));
    assertFalse(c.matches("origin/something/master"));
    assertTrue(c.matches("master"));
    assertFalse(c.matches("dev"));


    c.setAllowedBranches("origin/*/dev");

    assertFalse(c.matches("origintestdev"));
    assertTrue(c.matches("origin/test/dev"));
    assertFalse(c.matches("origin/test/release"));
    assertFalse(c.matches("origin/test/something/release"));

    c.setAllowedBranches("origin/*");

    assertTrue(c.matches("origin/master"));

    c.setAllowedBranches("**/magnayn/*");

    assertTrue(c.matches("origin/magnayn/b1"));
    assertTrue(c.matches("remote/origin/magnayn/b1"));
    assertTrue(c.matches("remotes/origin/magnayn/b1"));

    c.setAllowedBranches("*/my.branch/*");

    assertTrue(c.matches("origin/my.branch/b1"));
    assertFalse(c.matches("origin/my-branch/b1"));
    assertFalse(c.matches("remote/origin/my.branch/b1"));
    assertTrue(c.matches("remotes/origin/my.branch/b1"));

    c.setAllowedBranches("**");

    assertTrue(c.matches("origin/my.branch/b1"));
    assertTrue(c.matches("origin/my-branch/b1"));
    assertTrue(c.matches("remote/origin/my.branch/b1"));
    assertTrue(c.matches("remotes/origin/my.branch/b1"));

    c.setAllowedBranches("*");

    assertTrue(c.matches("origin/x"));
    assertFalse(c.matches("origin/my-branch/b1"));
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

    BitBucketPPRRepositoryPushActionFilter c =
        new BitBucketPPRRepositoryPushActionFilter(false, "${master}");

    assertTrue(c.matches("origin/master", env));
    assertFalse(c.matches("origin/something/master", env));
    assertTrue(c.matches("master", env));
    assertFalse(c.matches("dev", env));

    c.setAllowedBranches("${origin}/*/${dev}");

    assertFalse(c.matches("origintestdev", env));
    assertTrue(c.matches("origin/test/dev", env));
    assertFalse(c.matches("origin/test/release", env));
    assertFalse(c.matches("origin/test/something/release", env));

    c.setAllowedBranches("${origin}/*");

    assertTrue(c.matches("origin/master", env));

    c.setAllowedBranches("**/${magnayn}/*");

    assertTrue(c.matches("origin/magnayn/b1", env));
    assertTrue(c.matches("remote/origin/magnayn/b1", env));

    c.setAllowedBranches("*/${mybranch}/*");

    assertTrue(c.matches("origin/my.branch/b1", env));
    assertFalse(c.matches("origin/my-branch/b1", env));
    assertFalse(c.matches("remote/origin/my.branch/b1", env));

    c.setAllowedBranches("${anyLong}");

    assertTrue(c.matches("origin/my.branch/b1", env));
    assertTrue(c.matches("origin/my-branch/b1", env));
    assertTrue(c.matches("remote/origin/my.branch/b1", env));

    c.setAllowedBranches("${anyShort}");

    assertTrue(c.matches("origin/x", env));
    assertFalse(c.matches("origin/my-branch/b1", env));

    c.setAllowedBranches("${anyEmpty}");

    assertTrue(c.matches("origin/my.branch/b1", env));
    assertTrue(c.matches("origin/my-branch/b1", env));
    assertTrue(c.matches("remote/origin/my.branch/b1", env));
  }

  @Test
  public void testUsesRefsHeads() {
    BitBucketPPRRepositoryPushActionFilter c =
        new BitBucketPPRRepositoryPushActionFilter(false, "refs/heads/j*n*");

    assertTrue(c.matches("refs/heads/jenkins"));
    assertTrue(c.matches("refs/heads/jane"));
    assertTrue(c.matches("refs/heads/jones"));
    assertFalse(c.matches("origin/jenkins"));
    assertFalse(c.matches("remote/origin/jane"));
  }

  @Test
  public void testUsesJavaPatternDirectlyIfPrefixedWithColon() {

    BitBucketPPRRepositoryPushActionFilter m =
        new BitBucketPPRRepositoryPushActionFilter(false, ":^(?!(origin/prefix)).*");

    assertTrue(m.matches("origin"));
    assertTrue(m.matches("origin/master"));
    assertTrue(m.matches("origin/feature"));
    assertFalse(m.matches("origin/prefix_123"));
    assertFalse(m.matches("origin/prefix"));
    assertFalse(m.matches("origin/prefix-abc"));
  }

  @Test
  public void testMatches_not_1() {
    BitBucketPPRRepositoryPushActionFilter c =
        new BitBucketPPRRepositoryPushActionFilter(false, "*/master");

    assertFalse(c.matches("master"));
  }

  @Test
  public void testMatches_not_2() {
    BitBucketPPRRepositoryPushActionFilter c =
        new BitBucketPPRRepositoryPushActionFilter(false, "develop, :^(?!master$).*");
    assertFalse(c.matches("master"));

    c.setAllowedBranches(":^(?!develop$).*");
    assertFalse(c.matches("develop"));
  }

  @Test
  public void testMatches_empty_branches() {
    String allowedBranches = "";
    BitBucketPPRRepositoryPushActionFilter c =
        new BitBucketPPRRepositoryPushActionFilter(false, allowedBranches);

    assertTrue(c.matches("master"));
    assertTrue(c.matches("develop"));
    assertTrue(c.matches("feature/new-stuff"));
  }


  @Test
  public void testUsesJavaPatternWithRepetition() {
    BitBucketPPRRepositoryPushActionFilter m =
        new BitBucketPPRRepositoryPushActionFilter(false, ":origin/release-\\d{8}");
    assertTrue(m.matches("origin/release-20150101"));
    assertFalse(m.matches("origin/release-2015010"));
    assertFalse(m.matches("origin/release-201501011"));
    assertFalse(m.matches("origin/release-20150101-something"));
  }

  @Test
  public void testUsesJavaPatternToExcludeMultipleBranches() {
    BitBucketPPRRepositoryPushActionFilter m =
        new BitBucketPPRRepositoryPushActionFilter(false, ":^(?!origin/master$|origin/develop$).*");

    assertTrue(m.matches("origin/branch1"));
    assertTrue(m.matches("origin/branch-2"));
    assertTrue(m.matches("origin/master123"));
    assertTrue(m.matches("origin/develop-123"));
    assertFalse(m.matches("origin/master"));
    assertFalse(m.matches("origin/develop"));
  }
}
