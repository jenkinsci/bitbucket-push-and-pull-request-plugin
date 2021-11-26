package io.jenkins.plugins.bitbucketpushandpullrequest.client.api;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import org.apache.http.HttpResponse;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.github.scribejava.core.model.Verb;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRBearerAuthorizationApiConsumerTest {

  @Ignore
  @Test
  public void testSend() throws Exception {
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
