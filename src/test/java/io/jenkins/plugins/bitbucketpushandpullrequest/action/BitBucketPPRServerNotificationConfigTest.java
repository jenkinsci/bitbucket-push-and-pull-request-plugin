/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2026, Christian Del Monte.
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

package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The endpoint and the credential are resolved together, from a single level. A level is used only
 * when it declares BOTH; a partial level is skipped, and if no level is complete the resolution is
 * refused. This keeps the credential bound to the endpoint it was configured with.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitBucketPPRServerNotificationConfigTest {

  @Test
  void usesTheTriggerWhenItDeclaresBothEndpointAndCredential() {
    BitBucketPPRServerNotificationConfig config = BitBucketPPRServerNotificationConfig.resolve(
        trigger("https://trigger.example", "trigger-cred"),
        global("https://global.example", "global-cred"));

    assertEquals("https://trigger.example", config.baseUrl());
    assertEquals("trigger-cred", config.credentialsId());
  }

  @Test
  void fallsBackToGlobalWhenTheTriggerPairIsIncomplete() {
    // The trigger has an endpoint but no credential: not a complete pair, so the level is skipped.
    BitBucketPPRServerNotificationConfig config = BitBucketPPRServerNotificationConfig.resolve(
        trigger("https://trigger.example", ""),
        global("https://global.example", "global-cred"));

    assertEquals("https://global.example", config.baseUrl());
    assertEquals("global-cred", config.credentialsId());
  }

  @Test
  void usesGlobalWhenTheTriggerDeclaresNothing() {
    BitBucketPPRServerNotificationConfig config = BitBucketPPRServerNotificationConfig.resolve(
        trigger("", ""),
        global("https://global.example", "global-cred"));

    assertEquals("https://global.example", config.baseUrl());
    assertEquals("global-cred", config.credentialsId());
  }

  @Test
  void refusesWhenNoLevelDeclaresACompletePair() {
    // Neither level declares an endpoint AND a credential together.
    assertThrows(RuntimeException.class, () -> BitBucketPPRServerNotificationConfig.resolve(
        trigger("", ""),
        global("https://global.example", "")));
  }

  private static BitBucketPPRTrigger trigger(String propagationUrl, String credentialsId) {
    BitBucketPPRTrigger trigger = mock(BitBucketPPRTrigger.class);
    when(trigger.getPropagationUrl()).thenReturn(propagationUrl);
    when(trigger.getCredentialsId()).thenReturn(credentialsId);
    return trigger;
  }

  private static BitBucketPPRPluginConfig global(String propagationUrl, String credentialsId) {
    BitBucketPPRPluginConfig global = mock(BitBucketPPRPluginConfig.class);
    when(global.getPropagationUrl()).thenReturn(propagationUrl);
    when(global.getCredentialsId()).thenReturn(credentialsId);
    return global;
  }
}
