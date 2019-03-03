package io.jenkins.plugins.bitbucketpushandpullrequest.model;

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConsts.*;

import javax.naming.OperationNotSupportedException;

import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPRNewPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.old.BitBucketPPROldPost;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerPayload;


public class BitBucketPayloadFactory {
  public static BitBucketPPRPayload getInstance(BitBucketPPREvent bitbucketEvent) throws OperationNotSupportedException {
    BitBucketPPRPayload payload = null;
    
    if (REPOSITORY_EVENT.equalsIgnoreCase(bitbucketEvent.getEvent())) {
      if (REPOSITORY_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPRNewPayload();
      } else if (REPOSITORY_POST.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPROldPost();
      } else if (REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction())) {
        payload = new BitBucketPPRServerPayload();
      }
    } else if (PULL_REQUEST_EVENT.equals(bitbucketEvent.getEvent())) {
        payload = new BitBucketPPRNewPayload();
    }

    if (payload == null) {
      throw new OperationNotSupportedException(
          "No processor found for bitbucket event " + bitbucketEvent.getEvent());
    }
    
    
    return payload;
  }

}
