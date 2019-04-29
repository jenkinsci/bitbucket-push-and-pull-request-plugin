/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2019, CloudBees, Inc.
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
import java.util.stream.Collectors;

import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;


@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRJobProbeTest {
  Gson gson = new Gson();

  BitBucketPPRJobProbe jobProbe;

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
  public void testGetRemotesAsList() throws Exception {
    BitBucketPPRAction bitbucketAction = mock(BitBucketPPRAction.class);

    List<String> remotes = new ArrayList<>();
    remotes.add("https://some-user@bitbucket.org/theveryjenkins/test.git");
    remotes.add("git@bitbucket.org:theveryjenkins/test.git");
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
