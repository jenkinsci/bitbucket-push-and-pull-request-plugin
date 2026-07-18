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
package io.jenkins.plugins.bitbucketpushandpullrequest.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.plugins.git.BranchSpec;

import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRRepositoryNotParsedException;

public class BitBucketPPRUtils {

  private static final Logger logger = Logger.getLogger(BitBucketPPRUtils.class.getName());

  public static final String BB_WORKSPACE = "workspace";
  public static final String BB_REPOSITORY = "repository";

  private static final Set<String> nonHttpsWarnedOrigins = ConcurrentHashMap.newKeySet();

  // The request is still sent: setups terminating TLS on a trusted reverse proxy in front of
  // Bitbucket are legitimate, so a non-https URL is the operator's call, not an error. Warned
  // once per origin (scheme://host[:port]) until Jenkins is restarted or the plugin is reloaded:
  // notification URLs embed the commit hash, so keying on the full URL would warn on every
  // commit and grow the set without bound. Logging the origin instead of the full URL also
  // keeps paths, query strings and user-info out of the log.
  public static void warnIfNotHttps(String url) {
    if (url == null || url.regionMatches(true, 0, "https:", 0, 6)) {
      return;
    }
    String origin = originOf(url);
    if (nonHttpsWarnedOrigins.add(origin)) {
      logger.warning(() -> "The Bitbucket URL at " + origin
          + " is not https: the Authorization header travels unencrypted."
          + " Use an https URL unless TLS is terminated by a trusted proxy.");
    }
  }

  // Extracts scheme://host[:port] by hand instead of via java.net.URI: URI.getHost() is null
  // for authorities URI cannot model (hostnames with underscores, an invalid port such as the
  // ":-1" a portless base URL used to produce), and any fallback to the full URL would bring
  // back the per-commit key. Cutting at the end of the authority keeps the key bounded and
  // commit-free for every input; user-info is stripped so it can never reach the log.
  private static final ConcurrentMap<String, Integer> lastHttpErrorByOrigin =
      new ConcurrentHashMap<>();

  // A notification that does not succeed (rotated token, revoked credential, wrong repository, a
  // misconfigured proxy answering with a redirect) throws no exception: the transport call
  // succeeded, so without this warning the failure is visible only at FINEST. Unlike the static
  // non-https configuration, this is a dynamic error, so it is warned once per continuous
  // episode: the first non-successful status per origin warns, a repeat of the same status stays
  // silent, a status change warns again, and a successful response clears the state so the next
  // incident is a new episode. The response body is logged separately at FINE, sanitized and
  // truncated, so control characters cannot forge multi-line log entries and an oversized reply
  // cannot flood the log.
  public static void warnOnHttpError(String url, int statusCode, String body) {
    String origin = originOf(url);
    if (statusCode >= 200 && statusCode < 300) {
      lastHttpErrorByOrigin.remove(origin);
      return;
    }
    Integer previous = lastHttpErrorByOrigin.put(origin, statusCode);
    if (!Objects.equals(previous, statusCode)) {
      logger.warning(() -> "Build status notification to " + origin + " was answered with HTTP "
          + statusCode + " instead of success");
      if (body != null && !body.isBlank()) {
        logger.fine(() -> "Response body of the HTTP " + statusCode + " from " + origin + ": "
            + sanitizeForLog(body));
      }
    }
  }

  private static String sanitizeForLog(String body) {
    String flat = body.replaceAll("\\p{Cntrl}", " ").trim();
    return flat.length() > 500 ? flat.substring(0, 500) + "..." : flat;
  }

  private static String originOf(String url) {
    int schemeEnd = url.indexOf("://");
    int authorityStart = schemeEnd < 0 ? 0 : schemeEnd + 3;
    int authorityEnd = url.length();
    for (int i = authorityStart; i < authorityEnd; i++) {
      char c = url.charAt(i);
      if (c == '/' || c == '?' || c == '#') {
        authorityEnd = i;
        break;
      }
    }
    String authority = url.substring(authorityStart, authorityEnd);
    int userInfoEnd = authority.lastIndexOf('@');
    if (userInfoEnd >= 0) {
      authority = authority.substring(userInfoEnd + 1);
    }
    String scheme = schemeEnd < 0 ? "" : url.substring(0, schemeEnd) + "://";
    return (scheme + authority).toLowerCase(Locale.ROOT);
  }

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

  public static boolean matchWithRegex(@NonNull String haystack, @NonNull String patternStr, EnvVars env) {
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
