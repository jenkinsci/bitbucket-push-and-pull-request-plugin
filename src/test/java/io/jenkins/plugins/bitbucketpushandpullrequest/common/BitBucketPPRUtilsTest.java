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
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Map;
import java.util.stream.Stream;
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
}