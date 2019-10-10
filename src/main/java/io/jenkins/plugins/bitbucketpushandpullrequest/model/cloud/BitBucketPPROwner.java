/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPROwner implements Serializable {
  private String type;

  @SerializedName("display_name")
  private String displayName;
  private String uuid;
  BitBucketPPRLinks links;
  private String nickname;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public BitBucketPPRLinks getLinks() {
    return this.links;
  }

  public void setLinks(BitBucketPPRLinks links) {
    this.links = links;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  @Override
  public String toString() {
    return "BitBucketPPROwner [type=" + type + ", displayName=" + displayName + ", uuid=" + uuid
        + ", links=" + links + ", nickname=" + nickname + "]";
  }
}
