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
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.HttpResponse;
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
 * authentication paths), must keep validating certificate chains, and must warn when the
 * credentials are sent over a non-https URL.
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
      HttpResponse response =
          new BitBucketPPRBasicAuthApiConsumer().send(credentials, Verb.POST, url, "{}");
      assertEquals(200, response.getStatusLine().getStatusCode());
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
  void sendWarnsWhenUrlIsNotHttps() throws Exception {
    HttpServer plainServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    plainServer.createContext("/", exchange -> {
      byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    });
    plainServer.start();

    String url = "http://localhost:" + plainServer.getAddress().getPort() + "/rest/build-status";

    List<LogRecord> records = new CopyOnWriteArrayList<>();
    Handler capture = new Handler() {
      @Override
      public void publish(LogRecord record) {
        records.add(record);
      }

      @Override
      public void flush() {}

      @Override
      public void close() {}
    };
    Logger consumerLogger = Logger.getLogger(BitBucketPPRBasicAuthApiConsumer.class.getName());
    consumerLogger.addHandler(capture);
    try {
      HttpResponse response =
          new BitBucketPPRBasicAuthApiConsumer().send(credentials, Verb.POST, url, "{}");
      assertEquals(200, response.getStatusLine().getStatusCode());
      assertTrue(records.stream().anyMatch(
          r -> Level.WARNING.equals(r.getLevel()) && r.getMessage().contains("not https")),
          () -> "expected a WARNING about the non-https URL, got: " + records);
    } finally {
      consumerLogger.removeHandler(capture);
      plainServer.stop(0);
    }
  }
}
