package io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.cloud;

import java.io.File;
import java.io.IOException;

import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;


public class BitBucketPPRPullRequestMergedCause extends BitBucketPPRPullRequestCause {
  public BitBucketPPRPullRequestMergedCause(File pollingLog, BitBucketPPRAction bitbucketAction)
      throws IOException {
    super(pollingLog, bitbucketAction);
  }

  @Override
  public String getShortDescription() {
    String pusher = bitbucketAction.getUser() != null ? bitbucketAction.getUser() : "";
    return "Started by user " + pusher + ": Bitbucket PPR: pull request merged";
  }
}
