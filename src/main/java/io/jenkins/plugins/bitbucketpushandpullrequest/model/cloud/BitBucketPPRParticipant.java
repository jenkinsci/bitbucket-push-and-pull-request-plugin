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

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import com.google.gson.annotations.SerializedName;


public class BitBucketPPRParticipant implements Serializable {
  @Serial
  private static final long serialVersionUID = 7546251977007143345L;
  private String type;
  private BitBucketPPRActor user;
  private String role;
  private boolean approved;
  private @SerializedName("participated_on") Date participatedOn;

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public BitBucketPPRActor getUser() {
    return user;
  }

  public void setUser(final BitBucketPPRActor user) {
    this.user = user;
  }

  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public boolean getApproved() {
    return approved;
  }

  public void setApproved(final boolean approved) {
    this.approved = approved;
  }

  public Date getParticipatedOn() {
    return (Date) participatedOn.clone();
  }

  public void setParticipatedOn(final Date participatedOn) {
    this.participatedOn = new Date(participatedOn.getTime());
  }
}
