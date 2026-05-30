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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import hudson.ExtensionList;
import io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class BitBucketPPRHookReceiverTest {

  @ParameterizedTest
  @MethodSource("paramsProvider")
  void execDecodeInputStream(String inputStream, String contentType, String expected)
      throws Exception {
    try (MockedStatic<ExtensionList> mocked = mockStatic(ExtensionList.class)) {
      mocked.when(
              (Verification) ExtensionList.lookupSingleton(BitBucketPPRPluginConfig.class))
          .thenReturn(mock(BitBucketPPRPluginConfig.class));
      assertEquals(expected,
          BitBucketPPRHookReceiver.decodeInputStream(inputStream, contentType));
    }
  }

  @Test
  void getInputStream_decompressesGzipBody() throws Exception {
    String expectedJson = "{\"pullrequest\":{\"id\":1}}";
    StaplerRequest request = mock(StaplerRequest.class);
    when(request.getHeader("Content-Encoding")).thenReturn("gzip");
    when(request.getInputStream()).thenReturn(toServletStream(gzip(expectedJson)));
    when(request.getContentType()).thenReturn("application/json");

    assertEquals(expectedJson, new BitBucketPPRHookReceiver().getInputStream(request));
  }

  @Test
  void getInputStream_doesNotDoubleDecompress_whenProxyAlreadyInflated() throws Exception {
    // Proxy already decompressed the body but left Content-Encoding: gzip header in place.
    // maybeGunzip() must detect plain JSON via magic bytes and skip decompression.
    String expectedJson = "{\"pullrequest\":{\"id\":1}}";
    StaplerRequest request = mock(StaplerRequest.class);
    when(request.getHeader("Content-Encoding")).thenReturn("gzip");
    when(request.getInputStream()).thenReturn(
        toServletStream(expectedJson.getBytes(StandardCharsets.UTF_8)));
    when(request.getContentType()).thenReturn("application/json");

    assertEquals(expectedJson, new BitBucketPPRHookReceiver().getInputStream(request));
  }

  @Test
  void getInputStream_handlesPlainJsonWithoutContentEncodingHeader() throws Exception {
    String expectedJson = "{\"pullrequest\":{\"id\":1}}";
    StaplerRequest request = mock(StaplerRequest.class);
    when(request.getHeader("Content-Encoding")).thenReturn(null);
    when(request.getInputStream()).thenReturn(
        toServletStream(expectedJson.getBytes(StandardCharsets.UTF_8)));
    when(request.getContentType()).thenReturn("application/json");

    assertEquals(expectedJson, new BitBucketPPRHookReceiver().getInputStream(request));
  }

  private static byte[] gzip(String input) throws java.io.IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gz = new GZIPOutputStream(baos)) {
      gz.write(input.getBytes(StandardCharsets.UTF_8));
    }
    return baos.toByteArray();
  }

  private static javax.servlet.ServletInputStream toServletStream(byte[] bytes) {
    return new javax.servlet.ServletInputStream() {
      private final ByteArrayInputStream delegate = new ByteArrayInputStream(bytes);
      @Override public int read() { return delegate.read(); }
      @Override public boolean isFinished() { return delegate.available() == 0; }
      @Override public boolean isReady() { return true; }
      @Override public void setReadListener(javax.servlet.ReadListener l) {}
    };
  }

  static Stream<Arguments> paramsProvider() {
    return Stream.of(
        arguments("here%20we%20are%2C%20isn%27t%20it%3F%3F", "abc",
            "here%20we%20are%2C%20isn%27t%20it%3F%3F"),
        arguments("here%20we%20are%2C%20isn%27t%20it%3F%3F", "",
            "here%20we%20are%2C%20isn%27t%20it%3F%3F"),
        arguments("payloadhere%20we%20are%2C%20isn%27t%20it%3F%3F",
            BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED,
            "payloadhere we are, isn't it??"),
        arguments(BitBucketPPRConst.PAYLOAD_PFX + "here%20we%20are%2C%20isn%27t%20it%3F%3F",
            BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED, "here we are, isn't it??"),
        arguments("", BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED, ""),
        arguments("here%20we%20are%2C%20isn%27t%20it%3F%3F",
            BitBucketPPRConst.APPLICATION_X_WWW_FORM_URLENCODED, "here we are, isn't it??")
    );
  }
}
