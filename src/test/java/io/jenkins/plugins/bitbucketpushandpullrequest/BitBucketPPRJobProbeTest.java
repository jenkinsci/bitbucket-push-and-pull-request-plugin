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

// import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class BitBucketPPRJobProbeTest {
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

  // private BitBucketPPRPayload getPayload() {
  // Gson gson = new Gson();
  // JsonReader reader = null;

  // try {
  // ClassLoader classloader = Thread.currentThread().getContextClassLoader();
  // InputStream is = classloader.getResourceAsStream("pullrequest_approved.json");
  // InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);O
  // reader = new JsonReader(isr);
  // } catch (Exception e) {
  // e.printStackTrace();
  // }

  // BitBucketPPRPayload payload = null;
  // try {
  // payload = gson.fromJson(reader, BitBucketPPRNewPayload.class);
  // } catch (JsonIOException e) {
  // e.printStackTrace();
  // } catch (JsonSyntaxException e) {
  // e.printStackTrace();
  // }

  // return payload;
  // }
}
