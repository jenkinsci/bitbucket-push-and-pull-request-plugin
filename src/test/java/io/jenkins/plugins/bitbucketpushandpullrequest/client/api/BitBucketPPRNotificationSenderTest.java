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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.github.scribejava.core.model.Verb;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpsServer;
import java.nio.file.Path;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies the production notification path ({@link BitBucketPPRNotificationSender}): TLS
 * behavior (certificate-chain validation, hostname verification, JVM trust-store configuration),
 * the auth headers on the less-exercised DELETE verb (used to revoke an approval when a build
 * fails), and the unsupported-verb contract. The deprecated consumer classes keep their own tests
 * for the legacy contract.
 */
@ExtendWith(MockitoExtension.class)
class BitBucketPPRNotificationSenderTest {

  private HttpsServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  private static StandardUsernamePasswordCredentials userPassCredentials() {
    StandardUsernamePasswordCredentials credentials =
        Mockito.mock(StandardUsernamePasswordCredentials.class, RETURNS_DEEP_STUBS);
    Mockito.when(credentials.getUsername()).thenReturn("a-user");
    Mockito.when(credentials.getPassword().getPlainText()).thenReturn("a-password");
    return credentials;
  }

  private static StringCredentials tokenCredentials() {
    StringCredentials credentials = Mockito.mock(StringCredentials.class, RETURNS_DEEP_STUBS);
    Mockito.when(credentials.getSecret().getPlainText()).thenReturn("a-secret-bearer-token");
    return credentials;
  }

  @Test
  void sendBearerRejectsUntrustedCertificate(@TempDir Path tmp) throws Exception {
    Path keystore = tmp.resolve("keystore.p12");
    TlsTestSupport.generateSelfSignedKeystore(keystore, "CN=localhost",
        "dns:localhost,ip:127.0.0.1");
    server = TlsTestSupport.startHttpsServer(keystore);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    SSLException exception = assertThrows(SSLException.class, () -> BitBucketPPRNotificationSender
        .sendBearer(tokenCredentials(), Verb.POST, url, "{}"));
    // The certificate carries localhost in its SAN, so the failure must come from the untrusted
    // chain, not from a hostname mismatch or an unrelated TLS problem.
    assertTrue(indicatesCertPathFailure(exception),
        () -> "expected a certificate path validation failure, got: " + exception);
  }

  @Test
  void sendBearerRejectsTrustedCertificateWithMismatchedHostname(@TempDir Path tmp)
      throws Exception {
    Path keystore = tmp.resolve("keystore.p12");
    TlsTestSupport.generateSelfSignedKeystore(keystore, "CN=bitbucket.test", "dns:bitbucket.test");
    server = TlsTestSupport.startHttpsServer(keystore);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    SSLContext original = SSLContext.getDefault();
    SSLContext.setDefault(TlsTestSupport.contextTrusting(keystore));
    try {
      assertThrows(SSLPeerUnverifiedException.class, () -> BitBucketPPRNotificationSender
          .sendBearer(tokenCredentials(), Verb.POST, url, "{}"));
    } finally {
      SSLContext.setDefault(original);
    }
  }

  @Test
  void sendBasicAuthHonoursJvmDefaultTlsConfiguration(@TempDir Path tmp) throws Exception {
    Path keystore = tmp.resolve("keystore.p12");
    TlsTestSupport.generateSelfSignedKeystore(keystore, "CN=localhost",
        "dns:localhost,ip:127.0.0.1");
    server = TlsTestSupport.startHttpsServer(keystore);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    SSLContext original = SSLContext.getDefault();
    SSLContext.setDefault(TlsTestSupport.contextTrusting(keystore));
    try {
      BitBucketPPRApiResponse response = BitBucketPPRNotificationSender
          .sendBasicAuth(userPassCredentials(), Verb.POST, url, "{}");
      assertEquals(200, response.statusCode());
      assertEquals("{}", response.body());
    } finally {
      SSLContext.setDefault(original);
    }
  }

  @Test
  void sendBasicAuthDeleteCarriesTheAuthHeaders(@TempDir Path tmp) throws Exception {
    Headers headers = deleteRequestHeaders(tmp,
        url -> BitBucketPPRNotificationSender.sendBasicAuth(userPassCredentials(), Verb.DELETE,
            url, ""));
    assertTrue(headers.getFirst("Authorization").startsWith("Basic "),
        () -> "expected a preemptive Basic Authorization header, got: "
            + headers.getFirst("Authorization"));
    assertEquals("nocheck", headers.getFirst("X-Atlassian-Token"));
  }

  @Test
  void sendBearerDeleteCarriesTheAuthHeaders(@TempDir Path tmp) throws Exception {
    Headers headers = deleteRequestHeaders(tmp,
        url -> BitBucketPPRNotificationSender.sendBearer(tokenCredentials(), Verb.DELETE, url,
            ""));
    assertTrue(headers.getFirst("Authorization").startsWith("Bearer "),
        () -> "expected a Bearer Authorization header, got: "
            + headers.getFirst("Authorization"));
    assertEquals("nocheck", headers.getFirst("X-Atlassian-Token"));
  }

  @Test
  void sendRejectsUnsupportedVerbs() {
    assertThrows(NoSuchMethodException.class, () -> BitBucketPPRNotificationSender
        .sendBasicAuth(userPassCredentials(), Verb.GET, "https://bitbucket-unused.test", ""));
  }

  // DELETE is production-reachable (approve revocation on failed builds) and shares the request
  // building with POST: these tests pin that the Authorization and X-Atlassian-Token headers do
  // not drift off the less-exercised verb.
  private Headers deleteRequestHeaders(Path tmp, DeleteCall call) throws Exception {
    Path keystore = tmp.resolve("keystore.p12");
    TlsTestSupport.generateSelfSignedKeystore(keystore, "CN=localhost",
        "dns:localhost,ip:127.0.0.1");
    AtomicReference<Headers> requestHeaders = new AtomicReference<>();
    server = TlsTestSupport.startHttpsServer(keystore, requestHeaders);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    SSLContext original = SSLContext.getDefault();
    SSLContext.setDefault(TlsTestSupport.contextTrusting(keystore));
    try {
      BitBucketPPRApiResponse response = call.send(url);
      assertEquals(200, response.statusCode());
    } finally {
      SSLContext.setDefault(original);
    }
    return requestHeaders.get();
  }

  @FunctionalInterface
  private interface DeleteCall {
    BitBucketPPRApiResponse send(String url) throws Exception;
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
