/*
 * The MIT License
 *
 * Copyright (c) 2018-2025 Christian Del Monte.
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
 */
package io.jenkins.plugins.bitbucketpushandpullrequest.receiver;

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.HOOK_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.net.URL;
import java.util.stream.Stream;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * End-to-end check of the HTTP status the webhook receiver returns (issue #384): a malformed
 * payload that cannot be turned into an action must be rejected with a 4xx BEFORE the request is
 * acknowledged, instead of being answered with a silent HTTP 200 and then dropped. Unsupported
 * events without a processor (e.g. a diagnostics ping) keep their 200 acknowledgement.
 */
@WithJenkins
class BitBucketPPRHookReceiverResponseStatusTest {

  private JenkinsRule j;

  @BeforeEach
  void setUp(JenkinsRule rule) {
    j = rule;
  }

  static Stream<Arguments> cases() {
    return Stream.of(
        arguments("pullrequest:created", "{}", 400, "cloud PR without pullrequest -> 400"),
        arguments("pullrequest:created", "{\"pullrequest\":{\"id\":1}}", 400,
            "cloud PR with pullrequest but no repository -> 400"),
        arguments("repo:push", "{}", 400, "cloud push without push -> 400"),
        arguments("repo:push", "{\"push\":{}}", 400, "cloud push with push but no changes -> 400"),
        arguments("repo:refs_changed", "{}", 400, "server push without repository -> 400"),
        arguments("diagnostics:ping", "{}", 200, "diagnostics ping -> 200 (acknowledged)"));
  }

  @ParameterizedTest(name = "{3}")
  @MethodSource("cases")
  void respondsWithExpectedStatus(String eventKey, String body, int expectedStatus, String desc)
      throws Exception {
    try (JenkinsRule.WebClient wc = j.createWebClient()) {
      wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
      WebRequest req =
          new WebRequest(new URL(wc.getContextPath() + HOOK_URL + "/"), HttpMethod.POST);
      req.setAdditionalHeader("x-event-key", eventKey);
      req.setAdditionalHeader("content-type", "application/json");
      req.setRequestBody(body);

      WebResponse resp = wc.getPage(req).getWebResponse();
      assertEquals(expectedStatus, resp.getStatusCode(), desc);
    }
  }
}
