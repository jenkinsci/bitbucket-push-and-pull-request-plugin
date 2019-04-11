package io.jenkins.plugins.bitbucketpushandpullrequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import hudson.model.Job;
import hudson.security.ACL;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, ACL.class, Logger.class})
public class BitBucketPPRJobProbeTest {
  Gson gson = new Gson();

  BitBucketPPRJobProbe jobProbe;

  // @Rule
  // public JenkinsRule jenkins = new JenkinsRule();

  @BeforeClass
  public static void beforeClass() {
    System.out.println("Starting test class" + BitBucketPPRJobProbeTest.class.getName());
  }

  @Before
  public void setUp() {
    System.out.println("Starting a test");
    jobProbe = new BitBucketPPRJobProbe();
  }

  @Test
  public void testTriggerMatchingJobs() {
    PowerMockito.mockStatic(Jenkins.class);
    
    
    PowerMockito.verifyStatic(Jenkins.class);

    
    jobProbe.triggerMatchingJobs(null, null);
    Jenkins.get();    
    
  }
  
  @Test
  public void testGetRemotesAsList() throws Exception {
    BitBucketPPRAction bitbucketAction = mock(BitBucketPPRAction.class);

    // PowerMockito.whenNew(BitBucketPPRAction.class).withNoArguments().thenReturn(bitbucketAction);

    List<String> remotes = new ArrayList<>();
    remotes.add("https://cdelmonte-zg@bitbucket.org/theveryjenkinsadventure/test-one.git");
    remotes.add("git@bitbucket.org:theveryjenkinsadventure/test-one.git");
    when(bitbucketAction.getScmUrls()).thenReturn(remotes);

    List<URIish> returnedList = remotes.stream().map(a -> {
      try {
        return new URIish(a);
      } catch (URISyntaxException e) {
        return null;
      }
    }).collect(Collectors.toList());

    assertEquals(returnedList, jobProbe.getRemotesAsList(bitbucketAction));
  }


  private BitBucketPPRPayload getPayload() {
    JsonReader reader = null;

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("pullrequest_approved.json");
      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
      reader = new JsonReader(isr);
    } catch (Exception e) {
      e.printStackTrace();
    }

    BitBucketPPRPayload payload = null;
    try {
      payload = gson.fromJson(reader, BitBucketPPRNewPayload.class);
    } catch (JsonIOException e) {
      e.printStackTrace();
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    }

    return payload;
  }

}
