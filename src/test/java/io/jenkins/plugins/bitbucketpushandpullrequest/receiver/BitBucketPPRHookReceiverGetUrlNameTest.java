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
package io.jenkins.plugins.bitbucketpushandpullrequest.receiver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import hudson.ExtensionList;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class BitBucketPPRHookReceiverGetUrlNameTest {

  @Test
  void testAGetUrlName() {
    try (MockedStatic<ExtensionList> mocked = mockStatic(ExtensionList.class)) {
      BitBucketPPRPluginConfig config = mock(BitBucketPPRPluginConfig.class);
      when(config.isHookUrlSet()).thenReturn(true);
      when(config.getHookUrl()).thenReturn("ABc");

      mocked.when(
              (Verification) ExtensionList.lookupSingleton(BitBucketPPRPluginConfig.class))
          .thenReturn(config);

      BitBucketPPRHookReceiver bitBucketPPRHookReceiver = new BitBucketPPRHookReceiver();
      assertEquals("abc", bitBucketPPRHookReceiver.getUrlName());
      assertNotEquals("ABc", bitBucketPPRHookReceiver.getUrlName());
    }
  }
}
