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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;

/**
 * Outcome of a Bitbucket API call, detached from the transport that produced it. The consumers
 * fully own the lifecycle of their HTTP client and response: by the time this value is returned,
 * every underlying resource has been consumed and released, so callers can hold it freely.
 */
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
