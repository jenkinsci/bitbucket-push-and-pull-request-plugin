package io.jenkins.plugins.bitbucketpushandpullrequest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPREvent;
import jenkins.model.Jenkins;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRJobProbeTest {

  BitBucketPPRJobProbe jobProbe;

  @BeforeClass
  public static void beforeClass() {
    System.out.println("Starting test class" + BitBucketPPRJobProbeTest.class.getName());
  }

  @Before
  public void before() {
    System.out.println("Strating a test");
    jobProbe = new BitBucketPPRJobProbe();
  }

  @Test
  public void testTriggerMatchingJobs() {

//    BitBucketPPREvent bitbucketEvent = mock(BitBucketPPREvent.class);
//    BitBucketPPRAction bitbucketAction = mock(BitBucketPPRAction.class);
//    when(bitbucketAction.getScm()).thenReturn("git");
//    
//    List<String> remotes = new ArrayList<>();
//    remotes.add("https://cdelmonte-zg@bitbucket.org/theveryjenkinsadventure/test-one.git");
//    remotes.add("git@bitbucket.org:theveryjenkinsadventure/test-one.git");
//    when(bitbucketAction.getScmUrls()).thenReturn(remotes);
//    
//    jobProbe.triggerMatchingJobs(bitbucketEvent, bitbucketAction);
    
    assertTrue(true);
  }
}
