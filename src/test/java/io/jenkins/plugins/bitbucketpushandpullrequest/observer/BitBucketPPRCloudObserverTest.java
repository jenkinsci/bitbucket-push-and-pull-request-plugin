package io.jenkins.plugins.bitbucketpushandpullrequest.observer;


import static org.junit.Assert.assertEquals;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.model.Run;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRCloudPayload;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRCloudObserverTest {
  public BitBucketPPRPayload payload;
  public BitBucketPPRHookEvent bitbucketEvent;

  @Before
  public void readPayload() {
    JsonReader reader = null;

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("./cloud/repo_push.json");
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Gson gson = new Gson();
    this.payload = gson.fromJson(reader, BitBucketPPRCloudPayload.class);

    try {
      this.bitbucketEvent = new BitBucketPPRHookEvent("repo:push");
    } catch (OperationNotSupportedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPushCloudObserver() throws Throwable {
    BitBucketPPRAction action = new BitBucketPPRRepositoryAction(payload);
    List<String> links = new ArrayList<>();
    links.add(
        "https://api.bitbucket.org/2.0/repositories/some-repository/some-repo/commit/09c4367c5bdbef7d7a28ba4cc2638488c2088d6b");

    assertEquals(links, action.getCommitLinks());

    BitBucketPPRPushCloudObserver spyObserver = Mockito.spy(BitBucketPPRPushCloudObserver.class);
    BitBucketPPREvent event = Mockito.mock(BitBucketPPREvent.class);
    BitBucketPPREventContext context = Mockito.mock(BitBucketPPREventContext.class);
    Run run = Mockito.mock(Run.class);

    Mockito.when(context.getAction()).thenReturn(action);

    Mockito.when(context.getRun()).thenReturn(run);
    Mockito.when(context.getAbsoluteUrl()).thenReturn("https://someURL");
    Mockito.when(event.getContext()).thenReturn(context);

    spyObserver.getNotification(event);
    spyObserver.setBuildStatusInProgress();
    Mockito.verify(spyObserver).setBuildStatusInProgress();
    Mockito.verify(spyObserver).callClient(
        "https://api.bitbucket.org/2.0/repositories/some-repository/some-repo/commit/09c4367c5bdbef7d7a28ba4cc2638488c2088d6b/statuses/build",
        "{\"key\": \"0\", \"url\": \"https://someURL\", \"state\": \"INPROGRESS\" }");
  }
}
