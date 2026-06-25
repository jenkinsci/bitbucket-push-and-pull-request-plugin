/*
 * The MIT License
 *
 * Copyright (c) 2018-2026 Christian Del Monte.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class SignatureUtilsTest {

  // Known-answer vector for HMAC-SHA256 webhook signatures (same scheme used by Bitbucket):
  // verified independently with `printf '%s' 'Hello, World!' | openssl dgst -sha256 -hmac ...`.
  private static final String SECRET = "It's a Secret to Everybody";
  private static final byte[] BODY = "Hello, World!".getBytes(StandardCharsets.UTF_8);
  private static final String SIGNATURE =
      "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

  @Test
  void validSignatureIsAccepted() {
    assertTrue(SignatureUtils.isValid(BODY, SIGNATURE, SECRET));
  }

  @Test
  void uppercaseHexAndPrefixAreAccepted() {
    assertTrue(SignatureUtils.isValid(BODY, SIGNATURE.toUpperCase(Locale.ROOT), SECRET));
  }

  @Test
  void emptyBodySignatureIsAccepted() {
    // printf '' | openssl dgst -sha256 -hmac "It's a Secret to Everybody"
    assertTrue(SignatureUtils.isValid(new byte[0],
        "sha256=66a0c074deaa0f489ead6537e0d32f9a344b90bbeda705b6ed45ecd3b413fb40", SECRET));
  }

  @Test
  void tamperedBodyIsRejected() {
    byte[] tampered = "Hello, World?".getBytes(StandardCharsets.UTF_8);
    assertFalse(SignatureUtils.isValid(tampered, SIGNATURE, SECRET));
  }

  @Test
  void wrongSecretIsRejected() {
    assertFalse(SignatureUtils.isValid(BODY, SIGNATURE, "some other secret"));
  }

  @Test
  void missingHeaderIsRejected() {
    assertFalse(SignatureUtils.isValid(BODY, null, SECRET));
    assertFalse(SignatureUtils.isValid(BODY, "", SECRET));
    assertFalse(SignatureUtils.isValid(BODY, "   ", SECRET));
  }

  @Test
  void unsupportedAlgorithmPrefixIsRejected() {
    assertFalse(SignatureUtils.isValid(BODY,
        "sha1=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17", SECRET));
  }

  @Test
  void malformedHexIsRejected() {
    assertFalse(SignatureUtils.isValid(BODY, "sha256=not-hex-at-all", SECRET));
    assertFalse(SignatureUtils.isValid(BODY, "sha256=", SECRET));
  }
}
