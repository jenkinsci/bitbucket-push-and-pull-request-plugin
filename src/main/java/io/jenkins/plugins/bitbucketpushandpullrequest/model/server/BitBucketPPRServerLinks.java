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


package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPRServerLinks implements Serializable {
  @Serial
  private static final long serialVersionUID = -8908433977563131277L;

  @SerializedName("clone")
  private List<BitBucketPPRServerClone> cloneProperty = new ArrayList<>();

  @SerializedName("self")
  private List<BitBucketPPRServerSelf> selfProperty = new ArrayList<>();

  public List<BitBucketPPRServerClone> getCloneProperty() {
    return new ArrayList<>(cloneProperty);
  }

  public void setCloneProperty(final List<BitBucketPPRServerClone> cloneProperty) {
    this.cloneProperty = cloneProperty;
  }

  public List<BitBucketPPRServerSelf> getSelfProperty() {
    return new ArrayList<>(selfProperty);
  }

  public void setSelfProperty(final List<BitBucketPPRServerSelf> selfProperty) {
    this.selfProperty = selfProperty;
  }
}
