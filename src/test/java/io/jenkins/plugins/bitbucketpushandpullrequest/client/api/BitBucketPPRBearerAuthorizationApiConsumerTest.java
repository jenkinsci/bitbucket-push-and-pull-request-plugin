package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.github.scribejava.core.model.Verb;
import org.apache.http.HttpResponse;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitBucketPPRBearerAuthorizationApiConsumerTest {

  @Disabled
  @Test
  void testSend() throws Exception {
    StringCredentials credentials = Mockito.mock(StringCredentials.class, RETURNS_DEEP_STUBS);
    Mockito.when(credentials.getSecret().getPlainText()).thenReturn("");

    String url = "";
    String payload = "";
    HttpResponse result;

    BitBucketPPRBearerAuthorizationApiConsumer testSubject =
        new BitBucketPPRBearerAuthorizationApiConsumer();
    result = testSubject.send(credentials, Verb.POST, url, payload);

    assertEquals(200, result.getStatusLine().getStatusCode());
  }
}
