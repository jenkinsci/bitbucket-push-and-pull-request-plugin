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


public class BitBucketPPRActor implements Serializable {
  private static final long serialVersionUID = 4266599260720106245L;

  @SerializedName("display_name")
  private String displayName;

  @SerializedName("account_id")
  private String accountId;
  private String type;
  private BitBucketPPRLinks links;
  private String nickname;
  private String uuid;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(final String accountId) {
    this.accountId = accountId;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public BitBucketPPRLinks getLinks() {
    return links;
  }

  public void setLinks(final BitBucketPPRLinks links) {
    this.links = links;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(final String nickname) {
    this.nickname = nickname;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(final String uuid) {
    this.uuid = uuid;
  }

  @Override
  public String toString() {
    return "BitBucketPPRActor [displayName=" + displayName + ", accountId=" + accountId + ", type="
        + type + ", links=" + links + ", nickname=" + nickname + ", uuid=" + uuid + "]";
  }
}
