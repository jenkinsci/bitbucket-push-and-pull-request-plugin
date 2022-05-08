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
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.ExtensionList;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRHookReceiverTest {

  @Test
  public void testDecodeInputStream1() throws Exception {
    String inputStream = "";
    String contentType = BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED;
    String expected = "";

    execDecodeImputStream(inputStream, contentType, expected);
  }

  @Test
  public void testDecodeInputStream2() throws Exception {
    String inputStream = "here%20we%20are%2C%20isn%27t%20it%3F%3F";
    String contentType = BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED;
    String expected = "here we are, isn't it??";

    execDecodeImputStream(inputStream, contentType, expected);
  }

  @Test
  public void testDecodeInputStream3() throws Exception {
    String inputStream = BitBucketPPRConst.PAYLOAD_PFX + "here%20we%20are%2C%20isn%27t%20it%3F%3F";
    String contentType = BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED;
    String expected = "here we are, isn't it??";

    execDecodeImputStream(inputStream, contentType, expected);
  }

  @Test
  public void testDecodeInputStream4() throws Exception {
    String inputStream = "payloadhere%20we%20are%2C%20isn%27t%20it%3F%3F";
    String contentType = BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED;
    String expected = "payloadhere we are, isn't it??";

    execDecodeImputStream(inputStream, contentType, expected);
  }

  @Test
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NP_NULL_PARAM_DEREF_NONVIRTUAL",
      justification = "I know what I'm doing")
  public void testDecodeInputStream5() throws Exception {
    String inputStream = "here%20we%20are%2C%20isn%27t%20it%3F%3F";
    String contentType = "";
    String expected = "here%20we%20are%2C%20isn%27t%20it%3F%3F";
    execDecodeImputStream(inputStream, contentType, expected);
  }

  @Test
  public void testDecodeInputStream6() throws Exception {
    String inputStream = "here%20we%20are%2C%20isn%27t%20it%3F%3F";
    String contentType = "abc";
    String expected = "here%20we%20are%2C%20isn%27t%20it%3F%3F";
    execDecodeImputStream(inputStream, contentType, expected);
  }

  

  private void execDecodeImputStream(String inputStream, String contentType, String expected)
      throws UnsupportedEncodingException {
    try (MockedStatic<ExtensionList> mocked = mockStatic(ExtensionList.class)) {
      mocked.when((Verification) ExtensionList.lookupSingleton(BitBucketPPRPluginConfig.class))
          .thenReturn(mock(BitBucketPPRPluginConfig.class));
      assertEquals(expected, BitBucketPPRHookReceiver.decodeInputStream(inputStream, contentType));
    }
  }
}
