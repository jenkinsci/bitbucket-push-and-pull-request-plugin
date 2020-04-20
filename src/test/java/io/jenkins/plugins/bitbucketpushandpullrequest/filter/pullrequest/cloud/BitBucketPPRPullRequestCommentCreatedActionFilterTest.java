package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.EnvVars;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRPullRequestCommentCreatedActionFilterTest {

  @Test
  public void testEmptyHayStack() {
    String allowedBranches = "master";
    
    String haystack = "";
    String pattern = "";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentCreatedActionFilter c =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);
    
    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  public void testEmptyPattern() {
    String allowedBranches = "master";

    String haystack = "Comment";
    String pattern = "";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentCreatedActionFilter c =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertTrue(c.hasInComment(haystack, env));
  }

  @Test
  public void testPattern001() {
    String allowedBranches = "master";

    String haystack = "I need to find a reason to X.Y 1#a";
    String pattern = "test";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentCreatedActionFilter c =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  public void testPattern002() {
    String allowedBranches = "master";

    String haystack = "I need to find a reason to X.Y 1#a";
    String pattern = "need";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentCreatedActionFilter c =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertTrue(c.hasInComment(haystack, env));
  }

  @Test
  public void testPattern003() {
    String allowedBranches = "master";

    String haystack = "I need to find a reason to X.Y 1#a";
    String pattern = "somestring";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentCreatedActionFilter c =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  public void testPattern004() {
    String allowedBranches = "master";

    String haystack = "I need to find fI a reason to X.Y 1#a";
    String pattern = "^A\\s";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentCreatedActionFilter c =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  public void testPattern005() {
    String allowedBranches = "master";

    String haystack = "I need to find fI a reason to X.Y 1#a";
    String pattern = "^I\\s";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentCreatedActionFilter c =
        new BitBucketPPRPullRequestCommentCreatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertTrue(c.hasInComment(haystack, env));
  }
}