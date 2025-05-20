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

package io.jenkins.plugins.bitbucketpushandpullrequest.model.server;

import java.io.Serial;
import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class BitBucketPPRServerProject implements Serializable {
  @Serial
  private static final long serialVersionUID = -9104674017591226510L;
  private String key;
  private String id;
  private String name;
  private BitBucketPPRServerLinks links;

  @SerializedName("public")
  private boolean isPublic;
  private String type;

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public BitBucketPPRServerLinks getLinks() {
    return links;
  }

  public void setLinks(BitBucketPPRServerLinks links) {
    this.links = links;
  }

  @Override
  public String toString() {
    return "BitBucketPPRServerProject [id=" + id + ", isPublic=" + isPublic + ", key=" + key + ", links=" + links
        + ", name=" + name + ", type=" + type + "]";
  }
}
