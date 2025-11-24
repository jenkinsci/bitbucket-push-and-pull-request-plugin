/*
 * The MIT License
 *
 * Copyright (c) 2018-2025 Christian Del Monte.
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

package io.jenkins.plugins.bitbucketpushandpullrequest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.naming.OperationNotSupportedException;
import org.junit.jupiter.api.Test;


class BitBucketPPREventTest {

  @Test
  void testRepositoryEvent() throws Exception {
    String event = "repo";
    String action = "push";
    BitBucketPPRHookEvent bitbucketEvent = createEvent(event, action);

    assertEquals(event, bitbucketEvent.getEvent());
    assertEquals(action, bitbucketEvent.getAction());
  }

  @Test
  void testRepositoryEventPushAction() throws Exception {
    String event = "repo";
    String action = "push";
    BitBucketPPRHookEvent bitbucketEvent = createEvent(event, action);

    assertEquals(event, bitbucketEvent.getEvent());
    assertEquals(action, bitbucketEvent.getAction());
  }

  @Test
  void testPullRequestEventCreated() throws Exception {
    String event = "pullrequest";
    String action = "created";
    BitBucketPPRHookEvent bitbucketEvent = createEvent(event, action);

    assertEquals(event, bitbucketEvent.getEvent());
    assertEquals(action, bitbucketEvent.getAction());
  }

  @Test
  void testPullRequestEventUpdated() throws Exception {
    String event = "pullrequest";
    String action = "updated";
    BitBucketPPRHookEvent bitbucketEvent = createEvent(event, action);

    assertEquals(event, bitbucketEvent.getEvent());
    assertEquals(action, bitbucketEvent.getAction());
  }

  @Test
  void testPullRequestEventApprovedAction() throws Exception {
    String event = "pullrequest";
    String action = "approved";
    BitBucketPPRHookEvent bitbucketEvent = createEvent(event, action);

    assertEquals(event, bitbucketEvent.getEvent());
    assertEquals(action, bitbucketEvent.getAction());
  }

  @Test
  void testUnknownEvent() {
    String event = "fake";
    String action = "created";
    assertThrows(OperationNotSupportedException.class, () ->
        createEvent(event, action));
  }

  @Test
  void testUnknownAction() {
    String event = "repo";
    String action = "fake";
    assertThrows(OperationNotSupportedException.class, () ->
        createEvent(event, action));
  }

  private BitBucketPPRHookEvent createEvent(String event, String action)
      throws Exception {
    return new BitBucketPPRHookEvent(event + ":" + action);
  }
}
