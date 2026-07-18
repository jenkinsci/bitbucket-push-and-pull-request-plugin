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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.github.scribejava.core.model.Verb;
import com.sun.net.httpserver.HttpsServer;
import java.nio.file.Path;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies the TLS behaviour of the Bearer-token API consumer. The Bearer token travels in the
 * Authorization header, so the connection it travels over must be authenticated. Before this was
 * enforced, the consumer disabled certificate and hostname validation unconditionally and would
 * have completed the request against any certificate.
 *
 * <ul>
 * <li>Certificate-chain validation: a server presenting a certificate that does not chain to the
 * trust store configured for the JVM (a freshly generated self-signed certificate) must fail the
 * handshake with a certificate path failure, so the token is never transmitted.</li>
 * <li>Hostname verification: a certificate that is trusted but does not carry the contacted host
 * in its subject alternative names must be rejected as well.</li>
 * </ul>
 *
 * <p>The warning for non-https URLs is emitted at the dispatch layer
 * ({@code DefaultBitBucketPPRClient}), see {@code BitBucketPPRUtilsTest}.
 */
@ExtendWith(MockitoExtension.class)
class BitBucketPPRBearerAuthorizationApiConsumerTest {

  private HttpsServer server;
  private StringCredentials credentials;

  @BeforeEach
  void mockCredentials() {
    credentials = Mockito.mock(StringCredentials.class, RETURNS_DEEP_STUBS);
    Mockito.when(credentials.getSecret().getPlainText()).thenReturn("a-secret-bearer-token");
  }

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void sendRejectsUntrustedCertificate() throws Exception {
    Path keystore = TlsTestSupport.localhostKeystore();
    server = TlsTestSupport.startHttpsServer(keystore);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    BitBucketPPRBearerAuthorizationApiConsumer testSubject =
        new BitBucketPPRBearerAuthorizationApiConsumer();

    SSLException exception = assertThrows(SSLException.class,
        () -> testSubject.send(credentials, Verb.POST, url, "{}"));
    // The certificate carries localhost in its SAN, so the failure must come from the untrusted
    // chain, not from a hostname mismatch or an unrelated TLS problem.
    assertTrue(indicatesCertPathFailure(exception),
        () -> "expected a certificate path validation failure, got: " + exception);
  }

  @Test
  void sendRejectsTrustedCertificateWithMismatchedHostname() throws Exception {
    Path keystore = TlsTestSupport.mismatchedHostKeystore();
    server = TlsTestSupport.startHttpsServer(keystore);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    BitBucketPPRBearerAuthorizationApiConsumer testSubject =
        new BitBucketPPRBearerAuthorizationApiConsumer();

    // Trust the certificate at the JVM default level, where createSystem() picks it up: the
    // chain then validates and the failure left to observe is the hostname mismatch.
    SSLContext original = SSLContext.getDefault();
    SSLContext.setDefault(TlsTestSupport.contextTrusting(keystore));
    try {
      assertThrows(SSLPeerUnverifiedException.class,
          () -> testSubject.send(credentials, Verb.POST, url, "{}"));
    } finally {
      SSLContext.setDefault(original);
    }
  }

  private static boolean indicatesCertPathFailure(Throwable throwable) {
    for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
      if (cause instanceof CertPathValidatorException
          || cause instanceof CertPathBuilderException) {
        return true;
      }
      String message = cause.getMessage();
      if (message != null && message.contains("PKIX")) {
        return true;
      }
    }
    return false;
  }
}
