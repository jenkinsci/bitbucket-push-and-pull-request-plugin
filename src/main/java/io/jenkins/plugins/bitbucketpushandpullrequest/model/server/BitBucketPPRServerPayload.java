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
import java.util.ArrayList;
import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRServerPayload implements BitBucketPPRPayload {
  @Serial
  private static final long serialVersionUID = -5088466617368578337L;
  private BitBucketPPRServerActor actor;
  private BitBucketPPRServerPullRequest pullRequest;
  private BitBucketPPRServerRepository repository;
  private final List<BitBucketPPRServerChange> changes = new ArrayList<>();
  private BitBucketPPRServerComment comment;

  @SuppressFBWarnings
  @Override
  public BitBucketPPRServerActor getServerActor() {
    return actor;
  }

  @SuppressFBWarnings
  @Override
  public BitBucketPPRServerPullRequest getServerPullRequest() {
    return pullRequest;
  }

  @SuppressFBWarnings
  @Override
  public BitBucketPPRServerRepository getServerRepository() {
    return repository;
  }

  @Override
  public List<BitBucketPPRServerChange> getServerChanges() {
    return new ArrayList<>(changes);
  }

  @Override
  public BitBucketPPRServerComment getServerComment() {
    return comment;
  }

  @Override
  public String toString() {
    return new com.google.gson.Gson().toJson(this);
  }
}
