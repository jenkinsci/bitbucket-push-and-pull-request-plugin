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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.github.scribejava.core.model.Verb;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Locale;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies that the Bearer-token API consumer validates the server's certificate chain: a server
 * presenting a certificate that does not chain to the trust store configured for the JVM (here, a
 * freshly generated self-signed certificate) must cause the TLS handshake to fail, so the Bearer
 * token is never transmitted. Before this was enforced, the consumer disabled certificate and
 * hostname validation unconditionally and would have completed the request against any
 * certificate.
 *
 * <p>This exercises certificate-chain validation specifically: the certificate carries
 * {@code localhost} in its SAN, so the handshake fails on the untrusted chain rather than on a
 * hostname mismatch. The same fix also restores hostname verification; asserting that in isolation
 * would require a separately trusted certificate with a non-matching name and is left as a
 * follow-up.
 */
@ExtendWith(MockitoExtension.class)
class BitBucketPPRBearerAuthorizationApiConsumerTest {

  private static final String STORE_PASS = "changeit";

  private HttpsServer server;

  @BeforeEach
  void startSelfSignedServer(@TempDir Path tmp) throws Exception {
    Path keystore = tmp.resolve("keystore.p12");
    generateSelfSignedKeystore(keystore);

    char[] pass = STORE_PASS.toCharArray();
    KeyStore ks = KeyStore.getInstance("PKCS12");
    try (InputStream in = Files.newInputStream(keystore)) {
      ks.load(in, pass);
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, pass);
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), null, null);

    server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
    server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
    server.createContext("/", exchange -> {
      byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    });
    server.start();
  }

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void sendRejectsUntrustedCertificate() {
    StringCredentials credentials = Mockito.mock(StringCredentials.class, RETURNS_DEEP_STUBS);
    Mockito.when(credentials.getSecret().getPlainText()).thenReturn("a-secret-bearer-token");

    String url = "https://localhost:" + server.getAddress().getPort() + "/rest/build-status";

    BitBucketPPRBearerAuthorizationApiConsumer testSubject =
        new BitBucketPPRBearerAuthorizationApiConsumer();

    assertThrows(SSLException.class,
        () -> testSubject.send(credentials, Verb.POST, url, "{}"));
  }

  private static void generateSelfSignedKeystore(Path keystore) throws Exception {
    String binary = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
        ? "keytool.exe"
        : "keytool";
    String keytool = Path.of(System.getProperty("java.home"), "bin", binary).toString();
    Process process = new ProcessBuilder(keytool,
        "-genkeypair",
        "-noprompt",
        "-alias", "test",
        "-keyalg", "RSA",
        "-keysize", "2048",
        "-validity", "1",
        "-dname", "CN=localhost",
        "-ext", "san=dns:localhost,ip:127.0.0.1",
        "-storetype", "PKCS12",
        "-keystore", keystore.toString(),
        "-storepass", STORE_PASS,
        "-keypass", STORE_PASS)
        .redirectErrorStream(true)
        .start();
    String output;
    try (InputStream in = process.getInputStream()) {
      output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    int exit = process.waitFor();
    if (exit != 0) {
      throw new IllegalStateException(
          "keytool failed to generate a self-signed keystore (exit " + exit + "):\n" + output);
    }
  }
}
