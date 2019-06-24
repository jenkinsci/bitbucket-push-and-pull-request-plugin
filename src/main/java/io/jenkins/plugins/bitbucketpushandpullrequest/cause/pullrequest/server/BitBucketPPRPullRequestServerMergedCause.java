package io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.server;

import java.io.File;
import java.io.IOException;

import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.pullrequest.cloud.BitBucketPPRPullRequestCause;


public class BitBucketPPRPullRequestServerMergedCause extends BitBucketPPRPullRequestServerCause {
  public BitBucketPPRPullRequestServerMergedCause(File pollingLog, BitBucketPPRAction bitbucketAction)
      throws IOException {
    super(pollingLog, bitbucketAction);
  }

  @Override
  public String getShortDescription() {
    String pusher = bitbucketAction.getUser() != null ? bitbucketAction.getUser() : "";
    return "Started by user " + pusher + ": Bitbucket PPR: pull request merged";
  }
}
