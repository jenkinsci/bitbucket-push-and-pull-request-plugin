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
package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;

public class BitBucketPPRObservable {
  private static final Logger logger = Logger.getLogger(BitBucketPPRObservable.class.getName());

  private List<BitBucketPPRObserver> observers = new ArrayList<>();

  public void addObserver(BitBucketPPRObserver observer) {
    this.observers.add(observer);
  }

  public void removeObserver(BitBucketPPRObserver observer) {
    this.observers.remove(observer);

  }

  public void notifyObservers(BitBucketPPREvent event) {
    if (observers == null) {
      return;
    }

    for (BitBucketPPRObserver observer : this.observers) {
      if (observer != null && event != null) {
        logger.log(Level.INFO, "Event: {0} for observer {1}",
            new String[] {event.toString(), observer.toString()});
        observer.getNotification(event);
      } else {
        logger.log(Level.INFO, "observer or event are null");
      }
    }
  }

  public List<BitBucketPPRObserver> getObservers() {
    return observers;
  }

}
