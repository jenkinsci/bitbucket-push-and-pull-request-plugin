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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.zip.GZIPInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Outcome of a Bitbucket API call, detached from the transport that produced it. The sender fully
 * owns the lifecycle of its HTTP client and response: by the time this value is returned, every
 * underlying resource has been consumed and released, so callers can hold it freely.
 */
@Restricted(NoExternalUse.class)
public record BitBucketPPRApiResponse(int statusCode, String body) {

  private static final int MAX_BODY_BYTES = 16 * 1024;

  /**
   * Materializes an Apache HttpClient response. Used as the {@code ResponseHandler} of
   * {@code CloseableHttpClient#execute(HttpUriRequest, ResponseHandler)}, the execution form that
   * releases the connection before returning.
   */
  public static BitBucketPPRApiResponse from(HttpResponse response) throws IOException {
    int statusCode = response.getStatusLine().getStatusCode();
    HttpEntity entity = response.getEntity();
    if (entity == null) {
      return new BitBucketPPRApiResponse(statusCode, "");
    }
    // The body only feeds logs, so it is read up to a fixed cap: an oversized reply (a proxy
    // error page, a misrouted endpoint) must not be buffered whole. The client is closed after
    // every call, so the unread remainder cannot poison a pooled connection.
    byte[] head;
    try (InputStream content = entity.getContent()) {
      head = content.readNBytes(MAX_BODY_BYTES);
    }
    return new BitBucketPPRApiResponse(statusCode, new String(head, charsetOf(entity)));
  }

  /**
   * Materializes a scribejava response with the same body cap as the Apache path. The stream is
   * decompressed when the reply declares {@code Content-Encoding: gzip}, mirroring what
   * {@code Response#getBody()} would do, but reading at most the cap.
   */
  public static BitBucketPPRApiResponse from(com.github.scribejava.core.model.Response response)
      throws IOException {
    InputStream stream = response.getStream();
    if (stream == null) {
      // A scribejava Response can also be built directly from a materialized String body, in
      // which case there is no stream to read: the same budget applies to the string.
      String body = response.getBody();
      return new BitBucketPPRApiResponse(response.getCode(), capUtf8(body == null ? "" : body));
    }
    boolean gzip = "gzip".equalsIgnoreCase(response.getHeader("Content-Encoding"));
    byte[] head;
    // Two resources on purpose: the GZIPInputStream constructor reads the gzip header eagerly
    // and throws on a malformed body, and in that case the wrapper resource is never
    // initialized. Declaring the raw stream as its own resource first guarantees it is closed
    // even when building the wrapper fails; scribejava's Response.close() would not close it.
    try (InputStream raw = stream;
        InputStream content = gzip ? new GZIPInputStream(raw) : raw) {
      head = content.readNBytes(MAX_BODY_BYTES);
    }
    return new BitBucketPPRApiResponse(response.getCode(),
        new String(head, StandardCharsets.UTF_8));
  }

  private static String capUtf8(String body) {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    if (bytes.length <= MAX_BODY_BYTES) {
      return body;
    }
    return new String(bytes, 0, MAX_BODY_BYTES, StandardCharsets.UTF_8);
  }

  private static Charset charsetOf(HttpEntity entity) {
    try {
      ContentType contentType = ContentType.get(entity);
      if (contentType != null && contentType.getCharset() != null) {
        return contentType.getCharset();
      }
    } catch (ParseException | UnsupportedCharsetException e) {
      // a malformed or unknown Content-Type header must not fail the notification
    }
    return StandardCharsets.UTF_8;
  }
}
