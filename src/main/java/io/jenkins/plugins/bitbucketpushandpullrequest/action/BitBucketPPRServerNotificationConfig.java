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

import static org.apache.commons.lang3.StringUtils.isBlank;

import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRTrigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;

/**
 * The notification endpoint and credential, resolved together as a pair from a single configuration
 * level: the trigger if it declares both, otherwise the global configuration if it declares both,
 * otherwise the notification is refused. Resolving them together, never one from the trigger and the
 * other from the global level, keeps the credential bound to the endpoint it is meant for.
 */
public record BitBucketPPRServerNotificationConfig(String baseUrl, String credentialsId) {

  public static BitBucketPPRServerNotificationConfig resolve(
      BitBucketPPRTrigger trigger, BitBucketPPRPluginConfig globalConfig) {
    if (!isBlank(trigger.getPropagationUrl()) && !isBlank(trigger.getCredentialsId())) {
      return new BitBucketPPRServerNotificationConfig(
          trigger.getPropagationUrl(), trigger.getCredentialsId());
    }
    if (!isBlank(globalConfig.getPropagationUrl()) && !isBlank(globalConfig.getCredentialsId())) {
      return new BitBucketPPRServerNotificationConfig(
          globalConfig.getPropagationUrl(), globalConfig.getCredentialsId());
    }
    throw new RuntimeException(
        "No Bitbucket server notification target is configured: a propagation URL and a credential "
            + "must be set together, on the trigger or globally.");
  }
}
