/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2020, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.event;

import java.util.logging.Level;
import java.util.logging.Logger;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRHandlerTemplate;

public class BitBucketPPRBuildStarted implements BitBucketPPREvent {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRBuildStarted.class.getName());

  BitBucketPPREventContext context;
  BitBucketPPRHandlerTemplate handler;

  @Override
  public void setContext(BitBucketPPREventContext context) {
    this.context = context;
  }

  @Override
  public void setEventHandler(BitBucketPPRHandlerTemplate handler) {
    this.handler = handler;

  }

  @Override
  public void runHandler() {
    try {
      handler.run(BitBucketPPREventType.BUILD_STARTED);
    } catch (Exception e) {
      LOGGER.log(Level.INFO, e.getMessage());
    }
  }

  @Override
  public BitBucketPPREventContext getContext() {
    return context;
  }

  @Override
  public String toString() {
    return "BitBucketPPRBuildStarted [context=" + context + ", handler=" + handler + "]";
  }
}
