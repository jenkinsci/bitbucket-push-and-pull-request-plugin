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

      List<String> warned = nonHttpsWarnings(records);
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

      List<String> warned = nonHttpsWarnings(records);
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

      List<String> warned = nonHttpsWarnings(records);
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

      assertEquals(List.of(), nonHttpsWarnings(records));
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

  private static List<String> nonHttpsWarnings(List<LogRecord> records) {
    return records.stream().filter(r -> Level.WARNING.equals(r.getLevel()))
        .map(LogRecord::getMessage).filter(m -> m != null && m.contains("not https")).toList();
  }
}