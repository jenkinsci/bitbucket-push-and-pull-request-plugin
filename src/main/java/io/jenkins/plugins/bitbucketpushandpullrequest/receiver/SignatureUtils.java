/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2026, Christian Del Monte.
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

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility for validating webhook request signatures.
 *
 * <p>When a secret is configured on a Bitbucket webhook, Bitbucket Cloud and Bitbucket Server /
 * Data Center sign every delivery with an {@code X-Hub-Signature} header of the form
 * {@code sha256=&lt;hex&gt;}, where {@code &lt;hex&gt;} is the HMAC-SHA256 of the request body
 * computed with the secret as key. The signature covers the payload before any transport
 * encoding, so it must be validated against the request body after gzip decompression but before
 * any URL decoding.
 */
final class SignatureUtils {

  private static final String SHA256_PREFIX = "sha256=";
  private static final String HMAC_SHA256 = "HmacSHA256";

  private SignatureUtils() {}

  /**
   * Returns true if {@code signatureHeader} is a well-formed {@code sha256=&lt;hex&gt;} signature
   * whose HMAC-SHA256 matches {@code body} under {@code secret}. A missing, blank or malformed
   * header never validates. The hex comparison is case-insensitive and constant-time.
   */
  static boolean isValid(
      @Nonnull byte[] body, @CheckForNull String signatureHeader, @Nonnull String secret) {
    if (StringUtils.isBlank(signatureHeader)
        || !StringUtils.startsWithIgnoreCase(signatureHeader, SHA256_PREFIX)) {
      return false;
    }
    String providedHex =
        signatureHeader.substring(SHA256_PREFIX.length()).trim().toLowerCase(Locale.ROOT);

    byte[] computed;
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
      computed = mac.doFinal(body);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      // HmacSHA256 is mandatory on every JVM and the key is non-blank by the caller's contract.
      throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
    }

    return MessageDigest.isEqual(
        toHex(computed).getBytes(StandardCharsets.US_ASCII),
        providedHex.getBytes(StandardCharsets.US_ASCII));
  }

  private static String toHex(@Nonnull byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
    }
    return sb.toString();
  }
}
