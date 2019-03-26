package io.jenkins.plugins.bitbucketpushandpullrequest.action;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRPayload;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerChange;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.server.BitBucketPPRServerClone;


public class BitBucketPPRServerRepositoryAction extends BitBucketPPRAction {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRAction.class.getName());

  public BitBucketPPRServerRepositoryAction(BitBucketPPRPayload payload) {
    super(payload);

    this.scm = payload.getServerRepository().getScmId();

    List<BitBucketPPRServerClone> clones =
        payload.getServerRepository().getLinks().getCloneProperty();

    for (BitBucketPPRServerClone clone : clones) {
      if (clone.getName().equalsIgnoreCase("http") || clone.getName().equalsIgnoreCase("ssh")) {
        this.scmUrls.add(clone.getHref());
      }
    }

    for (BitBucketPPRServerChange change : payload.getServerChanges()) {
      if (change.getRefId() != null) {
        this.branchName = change.getRef().getDisplayId();
        this.type = change.getType();
        break;
      }
    }

    LOGGER.log(Level.INFO,
        () -> "Received commit hook notification from server for branch: " + this.branchName);
    LOGGER.log(Level.INFO, () -> "Received commit hook type from server: " + this.type);
  }
}
