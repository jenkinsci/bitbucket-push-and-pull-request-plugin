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

import static io.jenkins.plugins.bitbucketpushandpullrequest.common.BitBucketPPRConst.HOOK_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;

import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;

import hudson.util.Secret;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;

/**
 * End-to-end check of the webhook signature validation (issue #360): when a webhook secret is
 * configured, requests must carry a valid {@code X-Hub-Signature} header (HMAC-SHA256 of the
 * request body) or be rejected with HTTP 403 before any payload processing. Without a configured
 * secret the previous behaviour is unchanged.
 */
@WithJenkins
class BitBucketPPRHookReceiverSignatureTest {

  private static final String CREDENTIALS_ID = "bitbucket-webhook-secret";
  private static final String WEBHOOK_SECRET = "It's a Secret to Everybody";

  // diagnostics:ping with an empty JSON object is acknowledged with 200 once authenticated
  // (see BitBucketPPRHookReceiverResponseStatusTest).
  private static final String PING_EVENT = "diagnostics:ping";
  private static final String PING_BODY = "{}";

  private JenkinsRule j;

  @BeforeEach
  void setUp(JenkinsRule rule) throws Exception {
    j = rule;
    SystemCredentialsProvider.getInstance().getCredentials().add(new StringCredentialsImpl(
        CredentialsScope.GLOBAL, CREDENTIALS_ID, "webhook secret",
        Secret.fromString(WEBHOOK_SECRET)));
    SystemCredentialsProvider.getInstance().save();
    BitBucketPPRPluginConfig.getInstance().setWebhookSecretCredentialsId(CREDENTIALS_ID);
  }

  @Test
  void signedRequestIsAccepted() throws Exception {
    assertEquals(200, post(PING_EVENT, PING_BODY.getBytes(StandardCharsets.UTF_8),
        signature(PING_BODY, WEBHOOK_SECRET), false));
  }

  @Test
  void unsignedRequestIsRejected() throws Exception {
    assertEquals(403, post(PING_EVENT, PING_BODY.getBytes(StandardCharsets.UTF_8), null, false));
  }

  @Test
  void wrongSignatureIsRejected() throws Exception {
    assertEquals(403, post(PING_EVENT, PING_BODY.getBytes(StandardCharsets.UTF_8),
        signature(PING_BODY, "some other secret"), false));
  }

  @Test
  void gzippedBodyValidatesAgainstUncompressedBytes() throws Exception {
    // Bitbucket signs the payload before any transport encoding: the signature is computed over
    // the uncompressed JSON even when the delivery arrives with Content-Encoding: gzip.
    assertEquals(200, post(PING_EVENT, gzip(PING_BODY),
        signature(PING_BODY, WEBHOOK_SECRET), true));
  }

  @Test
  void unresolvableCredentialFailsClosed() throws Exception {
    BitBucketPPRPluginConfig.getInstance().setWebhookSecretCredentialsId("no-such-credential");
    assertEquals(403, post(PING_EVENT, PING_BODY.getBytes(StandardCharsets.UTF_8),
        signature(PING_BODY, WEBHOOK_SECRET), false));
  }

  @Test
  void noConfiguredSecretIgnoresSignatureHeader() throws Exception {
    BitBucketPPRPluginConfig.getInstance().setWebhookSecretCredentialsId("");
    assertEquals(200, post(PING_EVENT, PING_BODY.getBytes(StandardCharsets.UTF_8),
        "sha256=0000000000000000000000000000000000000000000000000000000000000000", false));
  }

  @Test
  void signedButMalformedPayloadIsStillRejectedWithBadRequest() throws Exception {
    // Authentication passes, then payload validation (#384) still applies: 400, not 200.
    String body = "{}";
    assertEquals(400, post("pullrequest:created", body.getBytes(StandardCharsets.UTF_8),
        signature(body, WEBHOOK_SECRET), false));
  }

  private int post(String eventKey, byte[] body, String signatureHeader, boolean gzipped)
      throws Exception {
    URL url = new URL(j.getURL(), HOOK_URL + "/");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    try {
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty("x-event-key", eventKey);
      connection.setRequestProperty("content-type", "application/json");
      if (signatureHeader != null) {
        connection.setRequestProperty("x-hub-signature", signatureHeader);
      }
      if (gzipped) {
        connection.setRequestProperty("Content-Encoding", "gzip");
      }
      connection.getOutputStream().write(body);
      return connection.getResponseCode();
    } finally {
      connection.disconnect();
    }
  }

  private static String signature(String body, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    StringBuilder sb = new StringBuilder("sha256=");
    for (byte b : mac.doFinal(body.getBytes(StandardCharsets.UTF_8))) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private static byte[] gzip(String input) throws java.io.IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
      gz.write(input.getBytes(StandardCharsets.UTF_8));
    }
    return out.toByteArray();
  }
}
