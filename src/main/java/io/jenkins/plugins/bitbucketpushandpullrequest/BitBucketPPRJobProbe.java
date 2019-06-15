/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2019, CloudBees, Inc.
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

package io.jenkins.plugins.bitbucketpushandpullrequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import com.google.common.base.Objects;

import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPREvent;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.triggers.SCMTriggerItem;


public class BitBucketPPRJobProbe {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRJobProbe.class.getName());
  private BitBucketPPREvent bitbucketEvent;
  private BitBucketPPRAction bitbucketAction;

  public void triggerMatchingJobs(BitBucketPPREvent bitbucketEvent,
                                  BitBucketPPRAction bitbucketAction) {
    this.bitbucketEvent = bitbucketEvent;
    this.bitbucketAction = bitbucketAction;

    if (!("git".equals(bitbucketAction.getScm()) || "hg".equals(bitbucketAction.getScm()))) {
      throw new UnsupportedOperationException("Unsupported SCM type " + bitbucketAction.getScm());
    }

    // TODO: do we need it?
    Jenkins.get().getACL();

    try (ACLContext old = ACL.as(ACL.SYSTEM) ) {
      List<URIish> remotes = getRemotesAsList(bitbucketAction);
      LOGGER.log(Level.FINE, "Considering remote {0}", remotes);

      Jenkins.get().getAllItems(Job.class).stream().forEach(job -> {
        LOGGER.log(Level.FINE, "Considering candidate job {0}", job.getName());

        triggerScm(job, remotes, bitbucketAction);
      });
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Invalid repository URLs {0}\n{1}",
          new Object[] {bitbucketAction.getScmUrls(), e.getMessage()});
    }
  }

  List<URIish> getRemotesAsList(BitBucketPPRAction bitbucketAction) {
    return (List<URIish>) (bitbucketAction.getScmUrls()).stream().map(a -> {
      try {
        return new URIish(a);
      } catch (URISyntaxException e) {
        LOGGER.log(Level.WARNING, "Invalid URI {0}", e.getMessage());
        return null;
      }
    }).collect(Collectors.toList());
  }

  void triggerScm(Job<?, ?> job, List<URIish> remotes, BitBucketPPRAction bitbucketAction) {
    LOGGER.log(Level.FINE, "Considering to poke {0}", job.getFullDisplayName());
    Optional<BitBucketPPRTrigger> bitbucketTrigger = getBitBucketTrigger(job);
    List<SCM> scmTriggered = new ArrayList<>();
    Optional<SCMTriggerItem> item =
        Optional.ofNullable(SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job));

    bitbucketTrigger
        .ifPresent(trigger -> item.ifPresent(i -> i.getSCMs().stream().forEach(scmTrigger ->
        {

          //todo add check isPipelineMultibranch
          if(!isPrSourceBranchSameAsJobsBranch(job, bitbucketAction)){
            LOGGER.log(Level.FINE,"Skipping for job:"+job.getDisplayName());
            return;
          }

          LOGGER.log(Level.FINE,"Scheduling for job:"+job.getDisplayName());

          boolean isRemoteSet = false;
          for (URIish remote : remotes) {
            if (match(scmTrigger, remote)) {
              isRemoteSet = true;
              break;
            }
          }

          if (isRemoteSet && !hasBeenTriggered(scmTriggered, scmTrigger)) {
            scmTriggered.add(scmTrigger);
            LOGGER.log(Level.FINE, "Triggering trigger {0} for job {1}",
                new Object[] {trigger.getClass().getName(), job.getFullDisplayName()});
            try {
              trigger.onPost(bitbucketEvent, this.bitbucketAction);
            } catch (Exception e) {
              LOGGER.log(Level.WARNING, "Error: {0}", e.getMessage());
            }
          } else {
            LOGGER.log(Level.FINE, "{0} SCM doesn't match remote repo {1}",
                new Object[] {job.getName(), remotes});
          }
        })));
  }

  private boolean isPrSourceBranchSameAsJobsBranch(Job<?, ?> job, BitBucketPPRAction bitbucketAction) {
    final String displayName = job.getDisplayName();
    final String sourceBranchName = bitbucketAction
        .getPayload()
        .getPullRequest()
        .getSource()
        .getBranch()
        .getName();
    LOGGER.log(Level.FINE,"Job Name : " +displayName);
    LOGGER.log(Level.FINE,"sourceBranchName : " +sourceBranchName);

    return displayName.equals(sourceBranchName);
  }

  private Optional<BitBucketPPRTrigger> getBitBucketTrigger(Job<?, ?> job) {
    if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
      ParameterizedJobMixIn.ParameterizedJob<?, ?> pJob =
          (ParameterizedJobMixIn.ParameterizedJob<?, ?>) job;

      return getBitBucketTrigger(pJob);
    }

    return Optional.empty();
  }

  private Optional<BitBucketPPRTrigger> getBitBucketTrigger(ParameterizedJob<?, ?> job) {
    return job.getTriggers().values().stream().filter(e -> e instanceof BitBucketPPRTrigger)
        .findFirst().map(p -> (BitBucketPPRTrigger) p);
  }

  private boolean hasBeenTriggered(List<SCM> scmTriggered, SCM scmTrigger) {
    for (SCM scm : scmTriggered) {
      if (scm.equals(scmTrigger)) {
        LOGGER.log(Level.FINEST, "Has been triggered {0}", scmTrigger.getType());
        return true;
      }
    }

    return false;
  }

  private boolean match(SCM scm, URIish url) {
    if (scm instanceof GitSCM) {
      return matchGitScm(scm, url);
    } else if (scm instanceof MercurialSCM) {
      return matchMercurialScm(scm, url);
    }

    return false;
  }

  private boolean matchMercurialScm(SCM scm, URIish remote) {
    try {
      URI hgUri = new URI(((MercurialSCM) scm).getSource());

      LOGGER.log(Level.INFO, "Trying to match {0} ", hgUri.toString() + "<-->" + remote.toString());
      if (looselyMatches(hgUri, remote)) {
        LOGGER.info("Machted scm");
        return true;
      }
    } catch (URISyntaxException ex) {
      LOGGER.log(Level.SEVERE, "Could not parse jobSource uri: {0} ", ex);
    }

    return false;
  }

  private boolean matchGitScm(SCM scm, URIish remote) {
    for (RemoteConfig remoteConfig : ((GitSCM) scm).getRepositories()) {
      for (URIish urIish : remoteConfig.getURIs()) {
        LOGGER.log(Level.INFO, "Trying to match {0} ",
            urIish.toString() + "<-->" + remote.toString());
        if (GitStatus.looselyMatches(urIish, remote)) {
          LOGGER.info("Machted scm");
          return true;
        }
      }
    }

    return false;
  }

  // needed cause the ssh and https URI differs in Bitbucket Server.
  @Deprecated
  private URIish parseBitBucketUrIish(URIish urIish) {
    if (urIish.getPath().startsWith("/scm")) {
      urIish = urIish.setPath(urIish.getPath().substring(4));
    }
    return urIish;
  }

  private boolean looselyMatches(URI notifyUri, URIish repository) {
    boolean result = false;
    try {
      URI repositoryUri = new URI(repository.toString());
      result = Objects.equal(notifyUri.getHost(), repositoryUri.getHost())
          && Objects.equal(notifyUri.getPath(), repositoryUri.getPath())
          && Objects.equal(notifyUri.getQuery(), repositoryUri.getQuery());
    } catch (URISyntaxException ex) {
      LOGGER.log(Level.SEVERE, "Could not parse repository uri: {0}, {1}",
          new Object[] {repository, ex});
    }

    return result;
  }
}
