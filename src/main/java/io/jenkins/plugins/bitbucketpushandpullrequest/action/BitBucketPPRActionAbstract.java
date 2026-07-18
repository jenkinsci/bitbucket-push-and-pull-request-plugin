/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2025, Christian Del Monte.
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

import hudson.model.InvisibleAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.config.BitBucketPPRPluginConfig;
import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRPayloadPropertyNotFoundException;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class BitBucketPPRActionAbstract extends InvisibleAction {
  private String propagationUrl = "";

  /**
   * Returns {@code value} if it is non-null, otherwise throws a
   * {@link BitBucketPPRPayloadPropertyNotFoundException} naming the missing property. Action
   * constructors use this to reject a payload that is missing a property they need, turning a
   * cryptic downstream {@link NullPointerException} into a clear, catchable error.
   */
  protected static <T> T requirePayloadProperty(T value, String property)
      throws BitBucketPPRPayloadPropertyNotFoundException {
    if (value == null) {
      throw new BitBucketPPRPayloadPropertyNotFoundException(
          "The property '" + property + "' was not found in the JSON payload.");
    }
    return value;
  }

  public BitBucketPPRActionAbstract() {
    if (!isEmpty(getGlobalConfig().propagationUrl)) {
      this.setPropagationUrl(getGlobalConfig().propagationUrl);
    }
  }

  // Resolved per call, not pinned in a static field: a static would capture whichever config
  // instance exists when this class is first loaded and go stale for every later caller in the
  // same JVM, an order dependency that bites tests mocking getInstance() (same reasoning as the
  // receiver, PR #386).
  public BitBucketPPRPluginConfig getGlobalConfig() {
    return BitBucketPPRPluginConfig.getInstance();
  }

  public void setPropagationUrl(String propagationUrl) {
    this.propagationUrl = propagationUrl;
  }

  public String getPropagationUrl() {
    return propagationUrl;
  }
}
