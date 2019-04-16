package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.plugins.git.BranchSpec;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRRepositoryPushActionFilterTest {

  BranchSpec branchSpecMock;
  
  @Before
  public void setUp() {
    branchSpecMock = mock(BranchSpec.class);
  }

  
  @Test
  public void testMatches() {
    String allowedBranches = "develop,feature/*";
    BitBucketPPRRepositoryPushActionFilter classUnderTest =
        new BitBucketPPRRepositoryPushActionFilter(false, allowedBranches);

    when(branchSpecMock.matches(anyString())).thenReturn(true);
    assertTrue(classUnderTest.matches(branchSpecMock, allowedBranches));
  }
  
  @Test
  public void testMatches_not() {
    String allowedBranches = "develop,feature/*";
    BitBucketPPRRepositoryPushActionFilter classUnderTest =
        new BitBucketPPRRepositoryPushActionFilter(false, allowedBranches);

    when(branchSpecMock.matches(anyString())).thenReturn(false);
    assertFalse(classUnderTest.matches(branchSpecMock, allowedBranches));
  }
  
  @Test
  public void testMatches_empty_branches()
  {
    String allowedBranches = "";
    BitBucketPPRRepositoryPushActionFilter classUnderTest =
        new BitBucketPPRRepositoryPushActionFilter(false, allowedBranches);

    assertTrue(classUnderTest.matches(branchSpecMock, allowedBranches));
    verify(branchSpecMock, never()).matches(anyString());
  }
}
