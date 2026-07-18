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
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

/**
 * Outcome of a Bitbucket API call, detached from the transport that produced it. The consumers
 * fully own the lifecycle of their HTTP client and response: by the time this value is returned,
 * every underlying resource has been consumed and released, so callers can hold it freely.
 */
public record BitBucketPPRApiResponse(int statusCode, String body) {

  /**
   * Materializes an Apache HttpClient response. Used as the {@code ResponseHandler} of
   * {@code CloseableHttpClient#execute(HttpUriRequest, ResponseHandler)}, the execution form that
   * consumes the entity and releases the connection before returning.
   */
  public static BitBucketPPRApiResponse from(HttpResponse response) throws IOException {
    return new BitBucketPPRApiResponse(response.getStatusLine().getStatusCode(),
        response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity()));
  }
}
