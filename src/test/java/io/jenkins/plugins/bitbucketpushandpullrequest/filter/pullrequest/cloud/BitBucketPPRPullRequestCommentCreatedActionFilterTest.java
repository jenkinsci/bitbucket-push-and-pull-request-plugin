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

package io.jenkins.plugins.bitbucketpushandpullrequest.filter.pullrequest.cloud;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.EnvVars;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitBucketPPRPullRequestCommentCreatedActionFilterTest {

  @Test
  void testEmptyHayStack() {
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
  void testEmptyPattern() {
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
  void testPattern001() {
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
  void testPattern002() {
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
  void testPattern003() {
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
  void testPattern004() {
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
  void testPattern005() {
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