/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2025, Christian Del Monte.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package io.jenkins.plugins.bitbucketpushandpullrequest.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;

/**
 * Utility for transparent gzip decompression of webhook request bodies.
 *
 * <p>Bitbucket Cloud sometimes delivers webhook payloads with {@code Content-Encoding: gzip}.
 * Some fronting proxies decompress the body transparently but leave the header in place.
 * This utility sniffs the first two magic bytes ({@code 0x1f 0x8b}) to decide whether
 * decompression is actually needed, rather than trusting the header blindly.
 */
final class GzipUtils {

  private GzipUtils() {}

  /**
   * Wraps {@code in} in a {@link GZIPInputStream} if the stream starts with the gzip magic bytes
   * ({@code 0x1f 0x8b}), otherwise returns the stream unchanged via a {@link PushbackInputStream}.
   *
   * <p>Uses a fill-loop to read exactly two bytes so the check is safe over real sockets where a
   * single {@code read()} call may return fewer bytes than requested (e.g. across TCP segment
   * boundaries). An empty body ({@code read == 0}) is pushed back as a no-op and left to the
   * caller's blank-check.
   *
   * <p>The caller is responsible for closing the returned stream.
   */
  static InputStream maybeGunzip(@Nonnull InputStream in) throws IOException {
    PushbackInputStream pb = new PushbackInputStream(in, 2);
    byte[] magic = new byte[2];
    int read = 0, n;
    while (read < 2 && (n = pb.read(magic, read, 2 - read)) != -1) {
      read += n;
    }
    if (read > 0) {
      pb.unread(magic, 0, read);
    }
    boolean isGzip = read == 2 && (magic[0] & 0xFF) == 0x1f && (magic[1] & 0xFF) == 0x8b;
    return isGzip ? new GZIPInputStream(pb) : pb;
  }
}
