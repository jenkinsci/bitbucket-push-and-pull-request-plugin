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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BitBucketPPRUtilsTest {

  @ParameterizedTest
  @MethodSource("gitSSHRepos")
  void testExtractRepositoryNameFromSSHUri(String repository, String expectedResultWorkspace,
      String expectedResultRepository) {
    BitBucketPPRUtils utils = new BitBucketPPRUtils();
    Map<String, String> res = utils.extractRepositoryNameFromSSHUri(repository);

    assertEquals(expectedResultWorkspace, res.get(BitBucketPPRUtils.BB_WORKSPACE));
    assertEquals(expectedResultRepository, res.get(BitBucketPPRUtils.BB_REPOSITORY));
  }

  static Stream<Arguments> gitSSHRepos() {
    return Stream.of(
        arguments("git@bitbucket.org:work-space/reponame.git", "work-space", "reponame"),
        arguments("git@bitbucket.org:workspace/reponame.git", "workspace", "reponame"),
        arguments("git@bitbucket.org:work&&space/reponame.git", "work&&space", "reponame"));
  }

  @ParameterizedTest
  @MethodSource("gitHTTPSRepos")
  void testExtractRepositoryNameFromHTTPSUrl(String repository, String expectedResultWorkspace,
      String expectedResultRepository) throws Exception {
    BitBucketPPRUtils utils = new BitBucketPPRUtils();
    Map<String, String> res = utils.extractRepositoryNameFromHTTPSUrlForTest(repository);

    assertEquals(expectedResultWorkspace, res.get(BitBucketPPRUtils.BB_WORKSPACE));
    assertEquals(expectedResultRepository, res.get(BitBucketPPRUtils.BB_REPOSITORY));
  }

  static Stream<Arguments> gitHTTPSRepos() {
    return Stream.of(
        arguments("https://username@bitbucket.org/work-space/reponame", "work-space",
            "reponame"),
        arguments("https://username@bitbucket.org/workspace/reponame", "workspace", "reponame"),
        arguments("https://username@bitbucket.org/work&&space/reponame/", "work&&space",
            "reponame"));
  }

  // Each test uses hosts of its own so the once-per-origin state, which is static and shared
  // across the JVM, cannot leak between tests.
  @Test
  void warnIfNotHttpsWarnsOncePerOrigin() {
    List<LogRecord> records = new ArrayList<>();
    Handler capture = newCapturingHandler(records);
    Logger utilsLogger = Logger.getLogger(BitBucketPPRUtils.class.getName());
    utilsLogger.addHandler(capture);
    try {
      // Notification URLs embed the commit hash: distinct URLs on the same origin must warn
      // only once.
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket-once.test/rest/build-status/1.0/commits/aaa");
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket-once.test/rest/build-status/1.0/commits/bbb");
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket-again.test/rest/build-status/1.0/commits/aaa");

      List<String> warned = warningsContaining(records, "not https");
      assertEquals(2, warned.size(),
          () -> "expected one warning per distinct non-https origin, got: " + warned);
      assertTrue(warned.get(0).contains("bitbucket-once.test"));
      assertFalse(warned.get(0).contains("commits"),
          () -> "the warning must name the origin, not the full URL, got: " + warned.get(0));
      assertTrue(warned.get(1).contains("bitbucket-again.test"));
    } finally {
      utilsLogger.removeHandler(capture);
    }
  }

  @Test
  void warnIfNotHttpsDedupsUrlsJavaNetUriCannotParse() {
    List<LogRecord> records = new ArrayList<>();
    Handler capture = newCapturingHandler(records);
    Logger utilsLogger = Logger.getLogger(BitBucketPPRUtils.class.getName());
    utilsLogger.addHandler(capture);
    try {
      // URLs java.net.URI cannot model as host+port (URI.getHost() is null for both, without
      // throwing): hostnames with underscores, and the legacy malformed ":-1" a portless base
      // URL produced before getBaseUrl learned to omit the missing port. Both must still dedup
      // on the authority, never on the commit-bearing full URL.
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket-legacy.test:-1/rest/build-status/1.0/commits/aaa");
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket-legacy.test:-1/rest/build-status/1.0/commits/bbb");
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket_ci.internal/rest/build-status/1.0/commits/aaa");
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket_ci.internal/rest/build-status/1.0/commits/bbb");

      List<String> warned = warningsContaining(records, "not https");
      assertEquals(2, warned.size(),
          () -> "expected one warning per authority even for unparseable URLs, got: " + warned);
      assertFalse(warned.get(0).contains("commits"),
          () -> "the warning must never contain the URL path, got: " + warned.get(0));
      assertFalse(warned.get(1).contains("commits"),
          () -> "the warning must never contain the URL path, got: " + warned.get(1));
    } finally {
      utilsLogger.removeHandler(capture);
    }
  }

  @Test
  void warnIfNotHttpsStripsUserInfoAndNormalizesHostCase() {
    List<LogRecord> records = new ArrayList<>();
    Handler capture = newCapturingHandler(records);
    Logger utilsLogger = Logger.getLogger(BitBucketPPRUtils.class.getName());
    utilsLogger.addHandler(capture);
    try {
      BitBucketPPRUtils.warnIfNotHttps(
          "http://user:s3cret@Bitbucket-Private.test/rest/build-status/1.0/commits/aaa");
      BitBucketPPRUtils
          .warnIfNotHttps("http://bitbucket-private.test/rest/build-status/1.0/commits/bbb");

      List<String> warned = warningsContaining(records, "not https");
      assertEquals(1, warned.size(),
          () -> "case-variant hosts and user-info must collapse to one origin, got: " + warned);
      assertFalse(warned.get(0).contains("s3cret"),
          () -> "credentials in the URL must never reach the log, got: " + warned.get(0));
      assertTrue(warned.get(0).contains("bitbucket-private.test"));
    } finally {
      utilsLogger.removeHandler(capture);
    }
  }

  @Test
  void warnIfNotHttpsIsSilentForHttpsAndNull() {
    List<LogRecord> records = new ArrayList<>();
    Handler capture = newCapturingHandler(records);
    Logger utilsLogger = Logger.getLogger(BitBucketPPRUtils.class.getName());
    utilsLogger.addHandler(capture);
    try {
      BitBucketPPRUtils.warnIfNotHttps("https://bitbucket-secure.test/rest/build-status");
      BitBucketPPRUtils.warnIfNotHttps("HTTPS://bitbucket-secure-upper.test/rest/build-status");
      BitBucketPPRUtils.warnIfNotHttps(null);

      assertEquals(List.of(), warningsContaining(records, "not https"));
    } finally {
      utilsLogger.removeHandler(capture);
    }
  }

  private static Handler newCapturingHandler(List<LogRecord> records) {
    return new Handler() {
      @Override
      public void publish(LogRecord record) {
        records.add(record);
      }

      @Override
      public void flush() {}

      @Override
      public void close() {}
    };
  }

  private static List<String> warningsContaining(List<LogRecord> records, String needle) {
    return records.stream().filter(r -> Level.WARNING.equals(r.getLevel()))
        .map(LogRecord::getMessage).filter(m -> m != null && m.contains(needle)).toList();
  }

  @Test
  void warnOnHttpErrorWarnsPerEpisodeAndRearmsAfterSuccess() {
    List<LogRecord> records = new ArrayList<>();
    Handler capture = newCapturingHandler(records);
    Logger utilsLogger = Logger.getLogger(BitBucketPPRUtils.class.getName());
    utilsLogger.addHandler(capture);
    try {
      // A repeat of the same failing status is the same episode and stays silent, a status
      // change is new information, and a success clears the state: the next incident, even with
      // the same status as the first, must warn again instead of being suppressed forever.
      BitBucketPPRUtils.warnOnHttpError(
          "http://bitbucket-rejected.test/rest/build-status/1.0/commits/aaa", 401, "{\"e\":1}");
      BitBucketPPRUtils.warnOnHttpError(
          "http://bitbucket-rejected.test/rest/build-status/1.0/commits/bbb", 401, "{\"e\":1}");
      BitBucketPPRUtils.warnOnHttpError(
          "http://bitbucket-rejected.test/rest/build-status/1.0/commits/ccc", 403, "{\"e\":2}");
      BitBucketPPRUtils.warnOnHttpError(
          "http://bitbucket-rejected.test/rest/build-status/1.0/commits/ddd", 200, "ok");
      BitBucketPPRUtils.warnOnHttpError(
          "http://bitbucket-rejected.test/rest/build-status/1.0/commits/eee", 401, "{\"e\":1}");

      List<String> warned = warningsContaining(records, "instead of success");
      assertEquals(3, warned.size(),
          () -> "expected 401, 403 and the post-success 401 to warn, got: " + warned);
      assertTrue(warned.get(0).contains("401"));
      assertTrue(warned.get(0).contains("bitbucket-rejected.test"));
      assertFalse(warned.get(0).contains("commits"),
          () -> "the warning must name the origin, not the full URL, got: " + warned.get(0));
      assertFalse(warned.get(0).contains("{\"e\""),
          () -> "the body must not be part of the warning, got: " + warned.get(0));
      assertTrue(warned.get(1).contains("403"));
      assertTrue(warned.get(2).contains("401"));
    } finally {
      utilsLogger.removeHandler(capture);
    }
  }

  @Test
  void warnOnHttpErrorTreatsRedirectsAsNotSuccessful() {
    List<LogRecord> records = new ArrayList<>();
    Handler capture = newCapturingHandler(records);
    Logger utilsLogger = Logger.getLogger(BitBucketPPRUtils.class.getName());
    utilsLogger.addHandler(capture);
    try {
      // A 3xx means the notification was not accepted where it was sent (wrong base URL, a
      // redirecting proxy): only 2xx counts as success.
      BitBucketPPRUtils.warnOnHttpError(
          "http://bitbucket-redirect.test/rest/build-status/1.0/commits/aaa", 302, "");

      List<String> warned = warningsContaining(records, "instead of success");
      assertEquals(1, warned.size());
      assertTrue(warned.get(0).contains("302"));
    } finally {
      utilsLogger.removeHandler(capture);
    }
  }

  @Test
  void warnOnHttpErrorLogsTheSanitizedBodyAtFine() {
    List<LogRecord> records = new ArrayList<>();
    Handler capture = newCapturingHandler(records);
    Logger utilsLogger = Logger.getLogger(BitBucketPPRUtils.class.getName());
    Level originalLevel = utilsLogger.getLevel();
    utilsLogger.setLevel(Level.FINE);
    utilsLogger.addHandler(capture);
    try {
      BitBucketPPRUtils.warnOnHttpError("http://bitbucket-body.test/rest/build-status/1.0/x",
          500, "line1\r\nline2\u0007end\u2028tail");

      List<String> bodies = records.stream().filter(r -> Level.FINE.equals(r.getLevel()))
          .map(LogRecord::getMessage)
          .filter(m -> m != null && m.contains("Response body")).toList();
      assertEquals(1, bodies.size(), () -> "expected the body at FINE, got: " + records);
      assertFalse(bodies.get(0).contains("\n"),
          () -> "control characters must be sanitized, got: " + bodies.get(0));
      assertFalse(bodies.get(0).contains("\u2028"),
          () -> "unicode line separators must be sanitized too, got: " + bodies.get(0));
      assertTrue(bodies.get(0).contains("line1  line2 end tail"),
          () -> "expected each control or separator character replaced by a space, got: "
              + bodies.get(0));
    } finally {
      utilsLogger.removeHandler(capture);
      utilsLogger.setLevel(originalLevel);
    }
  }
}