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
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.junit.MockitoJUnitRunner;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRCrumbExclusionTest {
  private static final int SUCCESS_RESPONSE = 200;

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  @Test
  public void shouldNotRequireACrumbForTheBitbucketHookUrl() throws Exception {
    JenkinsRule.WebClient webClient = jenkins.createWebClient();
    WebRequest wrs =
        new WebRequest(new URL(webClient.getContextPath() + "bitbucket-hook/"), HttpMethod.POST);
    wrs.setAdditionalHeader("x-event-key", "pullrequest:created");
    wrs.setRequestBody(URLEncoder.encode(readPayloadScript("./cloud/pr_created.json"),
        StandardCharsets.UTF_8.toString()));
    wrs.setAdditionalHeader(BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED,
        BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED);
    wrs.setCharset("UTF-8");
    WebResponse resp = webClient.getPage(wrs).getWebResponse();
    
    assertEquals(SUCCESS_RESPONSE, resp.getStatusCode());
  }

  private String readPayloadScript(String path) throws Exception {
    String script = null;
    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream(path);
      script = IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return script;
  }
}
