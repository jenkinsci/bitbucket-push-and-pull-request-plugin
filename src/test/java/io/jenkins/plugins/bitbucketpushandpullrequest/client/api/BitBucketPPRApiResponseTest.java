/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2026, Christian Del Monte.
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

package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.scribejava.core.model.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;

/**
 * The body only feeds logs, so both materialization paths (Apache entity and scribejava
 * response) must read at most the fixed cap: an oversized reply must never be buffered whole.
 */
class BitBucketPPRApiResponseTest {

  private static final int CAP = 16 * 1024;

  @Test
  void fromApacheResponseCapsTheBody() throws Exception {
    byte[] oversized = new byte[CAP + 4096];
    java.util.Arrays.fill(oversized, (byte) 'a');
    BasicHttpResponse response =
        new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
    response.setEntity(new ByteArrayEntity(oversized));

    BitBucketPPRApiResponse materialized = BitBucketPPRApiResponse.from(response);

    assertEquals(200, materialized.statusCode());
    assertEquals(CAP, materialized.body().length());
  }

  @Test
  void fromScribejavaResponseCapsTheBody() throws Exception {
    byte[] oversized = new byte[CAP + 4096];
    java.util.Arrays.fill(oversized, (byte) 'b');
    Response response =
        new Response(503, "Service Unavailable", Map.of(), new ByteArrayInputStream(oversized));

    BitBucketPPRApiResponse materialized = BitBucketPPRApiResponse.from(response);

    assertEquals(503, materialized.statusCode());
    assertEquals(CAP, materialized.body().length());
  }

  @Test
  void fromScribejavaResponseDecompressesGzip() throws Exception {
    ByteArrayOutputStream compressed = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
      gzip.write("{\"error\": \"unauthorized\"}".getBytes(StandardCharsets.UTF_8));
    }
    Response response = new Response(401, "Unauthorized", Map.of("Content-Encoding", "gzip"),
        new ByteArrayInputStream(compressed.toByteArray()));

    BitBucketPPRApiResponse materialized = BitBucketPPRApiResponse.from(response);

    assertEquals(401, materialized.statusCode());
    assertTrue(materialized.body().contains("unauthorized"),
        () -> "expected the gzip body to be decompressed, got: " + materialized.body());
  }
}
