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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Helpers for the API-consumer TLS tests: self-signed keystores generated with the JDK's keytool,
 * an HTTPS test server backed by such a keystore, and an {@link SSLContext} that trusts the
 * generated certificate (to install as the JVM default via {@link SSLContext#setDefault} where a
 * test needs the certificate to be trusted).
 */
final class TlsTestSupport {

  static final String STORE_PASS = "changeit";

  private TlsTestSupport() {}

  static void generateSelfSignedKeystore(Path keystore, String dname, String san)
      throws Exception {
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
        "-dname", dname,
        "-ext", "san=" + san,
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

  static HttpsServer startHttpsServer(Path keystore) throws Exception {
    return startHttpsServer(keystore, new AtomicReference<>());
  }

  /** The headers of the last received request are published to {@code lastRequestHeaders}. */
  static HttpsServer startHttpsServer(Path keystore, AtomicReference<Headers> lastRequestHeaders)
      throws Exception {
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(loadKeystore(keystore), STORE_PASS.toCharArray());
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), null, null);

    HttpsServer server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
    server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
    server.createContext("/", exchange -> {
      lastRequestHeaders.set(exchange.getRequestHeaders());
      byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    });
    server.start();
    return server;
  }

  static SSLContext contextTrusting(Path keystore) throws Exception {
    Certificate certificate = loadKeystore(keystore).getCertificate("test");
    KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
    truststore.load(null, null);
    truststore.setCertificateEntry("test", certificate);
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(truststore);
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, tmf.getTrustManagers(), null);
    return sslContext;
  }

  private static KeyStore loadKeystore(Path keystore) throws Exception {
    KeyStore ks = KeyStore.getInstance("PKCS12");
    try (InputStream in = Files.newInputStream(keystore)) {
      ks.load(in, STORE_PASS.toCharArray());
    }
    return ks;
  }
}
