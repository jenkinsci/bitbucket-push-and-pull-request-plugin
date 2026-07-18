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
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies the TLS behaviour of the Basic-auth API consumer, mirroring
 * {@link BitBucketPPRBearerAuthorizationApiConsumerTest}: the client must follow the JSSE
 * configuration of the Jenkins JVM (so a Bitbucket instance using a private CA works on both
 * authentication paths) and must keep validating certificate chains.
 *
 * <p>The warning for non-https URLs is emitted at the visitor layer, see
 * {@code BitBucketPPRUtilsTest}.
 */
@ExtendWith(MockitoExtension.class)
class BitBucketPPRBasicAuthApiConsumerTest {

  private HttpsServer server;
  private StandardUsernamePasswordCredentials credentials;

  @BeforeEach
  void mockCredentials() {
    credentials = Mockito.mock(StandardUsernamePasswordCredentials.class, RETURNS_DEEP_STUBS);
    Mockito.when(credentials.getUsername()).thenReturn("a-user");
    Mockito.when(credentials.getPassword().getPlainText()).thenReturn("a-password");
  }

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void sendHonoursJvmDefaultTlsConfiguration(@TempDir Path tmp) throws Exception {
    Path keystore = tmp.resolve("keystore.p12");
    TlsTestSupport.generateSelfSignedKeystore(keystore, "CN=localhost",
        "dns:localhost,ip:127.0.0.1");
    server = TlsTestSupport.startHttpsServer(keystore);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    // With the certificate trusted at the JVM default level the request must succeed: this is
    // what useSystemProperties() restores, a plain HttpClientBuilder.create() client ignores the
    // JVM default context and would fail the handshake here.
    SSLContext original = SSLContext.getDefault();
    SSLContext.setDefault(TlsTestSupport.contextTrusting(keystore));
    try {
      BitBucketPPRApiResponse response =
          new BitBucketPPRBasicAuthApiConsumer().send(credentials, Verb.POST, url, "{}");
      assertEquals(200, response.statusCode());
      assertEquals("{}", response.body());
    } finally {
      SSLContext.setDefault(original);
    }
  }

  @Test
  void sendRejectsUntrustedCertificate(@TempDir Path tmp) throws Exception {
    Path keystore = tmp.resolve("keystore.p12");
    TlsTestSupport.generateSelfSignedKeystore(keystore, "CN=localhost",
        "dns:localhost,ip:127.0.0.1");
    server = TlsTestSupport.startHttpsServer(keystore);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    assertThrows(SSLException.class,
        () -> new BitBucketPPRBasicAuthApiConsumer().send(credentials, Verb.POST, url, "{}"));
  }

  @Test
  void sendDeleteCarriesTheAuthHeaders(@TempDir Path tmp) throws Exception {
    // DELETE is production-reachable (approve revocation on failed builds) and shares the
    // request building with POST: this pins that the Authorization and X-Atlassian-Token
    // headers do not drift off the less-exercised verb.
    Path keystore = tmp.resolve("keystore.p12");
    TlsTestSupport.generateSelfSignedKeystore(keystore, "CN=localhost",
        "dns:localhost,ip:127.0.0.1");
    AtomicReference<Headers> requestHeaders = new AtomicReference<>();
    server = TlsTestSupport.startHttpsServer(keystore, requestHeaders);

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    SSLContext original = SSLContext.getDefault();
    SSLContext.setDefault(TlsTestSupport.contextTrusting(keystore));
    try {
      BitBucketPPRApiResponse response =
          new BitBucketPPRBasicAuthApiConsumer().send(credentials, Verb.DELETE, url, "");
      assertEquals(200, response.statusCode());
    } finally {
      SSLContext.setDefault(original);
    }

    Headers headers = requestHeaders.get();
    assertTrue(headers.getFirst("Authorization").startsWith("Basic "),
        () -> "expected a preemptive Basic Authorization header, got: "
            + headers.getFirst("Authorization"));
    assertEquals("nocheck", headers.getFirst("X-Atlassian-Token"));
  }

  @Test
  void sendRejectsUnsupportedVerbs() {
    assertThrows(NoSuchMethodException.class, () -> new BitBucketPPRBasicAuthApiConsumer()
        .send(credentials, Verb.GET, "https://bitbucket-unused.test", ""));
  }
}
