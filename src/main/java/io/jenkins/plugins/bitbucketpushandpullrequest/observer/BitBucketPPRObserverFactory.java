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
package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.PULL_REQUEST_CLOUD_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.PULL_REQUEST_SERVER_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_CLOUD_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_EVENT;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_SERVER_PUSH;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;


public class BitBucketPPRObserverFactory {
  static final Logger LOGGER = Logger.getLogger(BitBucketPPRObserverFactory.class.getName());

  public static List<BitBucketPPRObserver> createObservers(BitBucketPPREvent bitbucketEvent)
      throws Exception {

    List<BitBucketPPRObserver> observers = new ArrayList<>();

    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())
        && REPOSITORY_CLOUD_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
      observers.add(new BitBucketPPRCloudObserver());
      return observers;
    }
    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())
        && REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
      observers.add(new BitBucketPPRServerObserver());
      return observers;
    }

    if (PULL_REQUEST_CLOUD_EVENT.equals(bitbucketEvent.getEvent())) {
      observers.add(new BitBucketPPRCloudObserver());
      return observers;
    }

    if (PULL_REQUEST_SERVER_EVENT.equals(bitbucketEvent.getEvent())) {
      observers.add(new BitBucketPPRServerObserver());
      return observers;
    }

    throw new BitBucketPPRObserverNotFoundException();
  }
}
