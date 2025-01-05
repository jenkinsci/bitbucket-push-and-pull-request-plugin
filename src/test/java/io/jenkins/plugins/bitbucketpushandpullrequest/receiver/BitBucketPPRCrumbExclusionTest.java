/*
 * The MIT License
 *
 * Copyright (c) 2021 CloudBees, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.HOOK_URL;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.junit.jupiter.MockitoExtension;

import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

@ExtendWith(MockitoExtension.class)
public class BitBucketPPRCrumbExclusionTest {
  private static final int SUCCESS_RESPONSE = 200;

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  @Test
  public void testTest() throws IOException {
    JenkinsRule.WebClient wc = jenkins.createWebClient();
    WebRequest req = new WebRequest(new URL(wc.getContextPath() + HOOK_URL + "/"), HttpMethod.POST);

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("./cloud/pr_updated.json");
    String jenkinsFileContent = IOUtils.toString(is, StandardCharsets.UTF_8);
    assertNotNull(jenkinsFileContent);

    req.setAdditionalHeader("x-event-key", "pullrequest:updated");
    req.setAdditionalHeader("content-type", "application/json");
    req.setRequestBody(jenkinsFileContent);

    WebResponse resp = wc.getPage(req).getWebResponse();
    assertNotNull(resp.getContentAsString());
    assertEquals(SUCCESS_RESPONSE, resp.getStatusCode());
  }

}
