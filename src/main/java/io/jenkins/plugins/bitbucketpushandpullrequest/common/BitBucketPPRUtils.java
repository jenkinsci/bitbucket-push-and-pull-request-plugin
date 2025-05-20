/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2020, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import hudson.EnvVars;
import hudson.plugins.git.BranchSpec;

import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRRepositoryNotParsedException;

public class BitBucketPPRUtils {

  private static final Logger logger = Logger.getLogger(BitBucketPPRUtils.class.getName());

  public static final String BB_WORKSPACE = "workspace";
  public static final String BB_REPOSITORY = "repository";

  public static boolean matches(String allBranches, String branchName, EnvVars env) {
    String allowedBranchesPattern = allBranches != null ? allBranches : "";

    logger.fine("Following allowed branches patterns are set: " + allowedBranchesPattern);
    logger.fine("The branchName in action is: " + branchName);
    logger.fine("The environment variables are: " + env);

    BiFunction<List<String>, BiPredicate<String, EnvVars>, List<String>> filter = (List<String> list,
        BiPredicate<String, EnvVars> p) -> {
      List<String> results = new ArrayList<>();
      for (String t : list) {
        if (p.test(t, env)) {
          results.add(t);
        }
      }
      return results;
    };

    List<String> matchedBranches = filter.apply(Arrays.asList(allowedBranchesPattern.split(",")),
        (String branchSpec, EnvVars envVar) -> {
          BranchSpec pattern = new BranchSpec(branchSpec.trim());
          if (envVar == null) {
            return pattern.matches(branchName);
          } else {
            return pattern.matches(branchName, envVar);
          }
        });

    if (matchedBranches.isEmpty()) {
      logger.info("no matches for allowed branches pattern: " + allowedBranchesPattern);
      return false;
    }

    matchedBranches.forEach((String s) -> logger.info("Matched branch: " + s));
    return true;
  }

  public static boolean matchWithRegex(@Nonnull String haystack, @Nonnull String patternStr, EnvVars env) {
    if (haystack == null || haystack.trim().isEmpty()) {
      logger.info("The comment from BB is null or it is empty");
      return false;
    }

    if (patternStr == null || patternStr.trim().isEmpty()) {
      logger.fine("The regex filter on the comment from BB is null or it is empty");
      return true;
    }

    logger.log(Level.FINEST, "Applying the pattern {0} to the comment {1}", new Object[] { patternStr, haystack });
    Pattern pattern = Pattern.compile(patternStr.trim(), Pattern.CASE_INSENSITIVE);
    return pattern.matcher(haystack.trim()).find();
  }

  public static PrintStream createLoggingProxyForErrors(final PrintStream realPrintStream) {
    return new PrintStream(realPrintStream) {
      public void print(final String string) {
        logger.severe(string);
      }

      public void println(final String string) {
        logger.severe(string);
      }
    };
  }

  public static Map<String, String> extractRepositoryNameFromHTTPSUrl(String url)
      throws BitBucketPPRRepositoryNotParsedException {
    String workspacePattern = "([^/]+)";
    Matcher matcher = Pattern.compile(workspacePattern).matcher(url);
    List<String> workspace = new ArrayList<>();
    List<String> repoSlug = new ArrayList<>();
    int x = 0;
    while (matcher.find()) {
      if (x == 2) {
        workspace.add(matcher.group(1));
      }
      if (x == 3) {
        repoSlug.add(matcher.group(1));
      }
      x++;
    }

    if (workspace.isEmpty() || repoSlug.isEmpty()) {
      throw new BitBucketPPRRepositoryNotParsedException();
    }
    Map<String, String> result = new HashMap<>();
    result.put(BB_WORKSPACE, workspace.get(0));
    result.put(BB_REPOSITORY, repoSlug.get(0));

    return result;
  }

  public Map<String, String> extractRepositoryNameFromSSHUri(final String url) {
    String repoNamePattern = "([^/]+)\\.git$";
    Matcher repoNameMatcher = Pattern.compile(repoNamePattern).matcher(url);
    List<String> repoNameResult = new ArrayList<>();
    while (repoNameMatcher.find()) {
      repoNameResult.add(repoNameMatcher.group(1));
    }
    if (repoNameResult.size() > 1) {
      logger.warning("An error has occurred matching the name of the bitbucket repository");
    }

    String workspacePattern = "([^:]+)/";
    Matcher workspaceNameMatcher = Pattern.compile(workspacePattern).matcher(url);
    List<String> workspaceNameResult = new ArrayList<>();
    while (workspaceNameMatcher.find()) {
      workspaceNameResult.add(workspaceNameMatcher.group(1));
    }
    if (workspaceNameResult.size() > 1) {
      logger.warning("An error has occurred matching the name of the bitbucket workspace");
    }

    Map<String, String> result = new HashMap<>();
    result.put(BB_WORKSPACE, workspaceNameResult.get(0));
    result.put(BB_REPOSITORY, repoNameResult.get(0));

    return result;
  }

  public Map<String, String> extractRepositoryNameFromHTTPSUrlForTest(String url)
      throws BitBucketPPRRepositoryNotParsedException {
    return BitBucketPPRUtils.extractRepositoryNameFromHTTPSUrl(url);
  }
}
