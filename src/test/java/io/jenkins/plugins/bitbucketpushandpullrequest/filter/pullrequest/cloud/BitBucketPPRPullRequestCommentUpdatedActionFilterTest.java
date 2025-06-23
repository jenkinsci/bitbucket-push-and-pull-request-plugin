package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.EnvVars;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitBucketPPRPullRequestCommentUpdatedActionFilterTest {

  @Test
  void testEmptyHayStack() {
    String allowedBranches = "master";

    String haystack = "";
    String pattern = "";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  void testEmptyPattern() {
    String allowedBranches = "master";

    String haystack = "Comment";
    String pattern = "";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertTrue(c.hasInComment(haystack, env));
  }

  @Test
  void testPattern001() {
    String allowedBranches = "master";

    String haystack = "I need to find a reason to X.Y 1#a";
    String pattern = "test";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  void testPattern002() {
    String allowedBranches = "master";

    String haystack = "I need to find a reason to X.Y 1#a";
    String pattern = "need";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertTrue(c.hasInComment(haystack, env));
  }

  @Test
  void testPattern003() {
    String allowedBranches = "master";

    String haystack = "I need to find a reason to X.Y 1#a";
    String pattern = "somestring";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  void testPattern004() {
    String allowedBranches = "master";

    String haystack = "I need to find fI a reason to X.Y 1#a";
    String pattern = "^A\\s";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertFalse(c.hasInComment(haystack, env));
  }

  @Test
  void testPattern005() {
    String allowedBranches = "master";

    String haystack = "I need to find fI a reason to X.Y 1#a";
    String pattern = "^I\\s";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertTrue(c.hasInComment(haystack, env));
  }

  @Test
  void testPattern006() {
    String allowedBranches = "master";

    String haystack = "I need to find fI a reason to X Y 1#a";
    String pattern = ".*X\\sY\\s1#a.*";
    EnvVars env = null;

    BitBucketPPRPullRequestCommentUpdatedActionFilter c =
        new BitBucketPPRPullRequestCommentUpdatedActionFilter();
    c.setAllowedBranches(allowedBranches);
    c.setCommentFilter(pattern);

    assertTrue(c.hasInComment(haystack, env));
  }
}
