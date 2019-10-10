package io.jenkins.plugins.bitbucketpushandpullrequest.processor;

import javax.annotation.Nonnull;
import io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRServerRepositoryAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;


public class BitBucketPPRRepositoryServerPayloadProcessor extends BitBucketPPRPayloadProcessor {

  public BitBucketPPRRepositoryServerPayloadProcessor(BitBucketPPRJobProbe jobProbe,
      BitBucketPPREvent bitbucketEvent) {
    super(jobProbe, bitbucketEvent);
  }

  @Override
  public void processPayload(BitBucketPPRPayload payload) {
    BitBucketPPRAction action = buildActionForJobs(payload);
    jobProbe.triggerMatchingJobs(bitbucketEvent, action);
  }

  private BitBucketPPRAction buildActionForJobs(@Nonnull BitBucketPPRPayload payload) {
    return new BitBucketPPRServerRepositoryAction(payload);
  }
}
