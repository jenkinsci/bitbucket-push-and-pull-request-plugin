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

package io.jenkins.plugins.bitbucketpushandpullrequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import com.google.common.base.Objects;

import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.triggers.Trigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.cloud.BitBucketPPREvent;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
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

    Jenkins.get().getACL();
    SecurityContext old = ACL.impersonate(ACL.SYSTEM);

    try {
      URIish remote = new URIish(bitbucketAction.getScmUrl());
      LOGGER.log(Level.FINE, "Considering remote {0}", remote);
      
      for (Job<?, ?> job : Jenkins.get().getAllItems(Job.class)) {
        LOGGER.log(Level.FINE, "Considering candidate job {0}", job.getName());

        BitBucketPPRTrigger bitbucketTrigger = getBitBucketTrigger(job);
        if (bitbucketTrigger != null) {
          List<SCM> scmTriggered = getTriggeredScm(job, bitbucketTrigger, remote);
          LOGGER.log(Level.FINE, "Considering to poke {0}", job.getFullDisplayName());
        }
      }

    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Invalid repository URL {0}", bitbucketAction.getScmUrl());
      LOGGER.warning(e.getMessage());
    } finally {
      SecurityContextHolder.setContext(old);
    }
  }

  private List<SCM> getTriggeredScm(Job<?, ?> job, BitBucketPPRTrigger bitbucketTrigger,
      URIish remote) {
    List<SCM> scmTriggered = new ArrayList<>();

    try {
      SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
      for (SCM scmTrigger : item.getSCMs()) {
        if (match(scmTrigger, remote) && !hasBeenTriggered(scmTriggered, scmTrigger)) {
          scmTriggered.add(scmTrigger);

          bitbucketTrigger.onPost(bitbucketEvent, bitbucketAction);
        } else {
          LOGGER.log(Level.FINE, "{0} SCM doesn't match remote repo {1}",
              new Object[] {job.getName(), remote});
        }
      }
    } catch (NullPointerException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
    }

    return scmTriggered;
  }

  private BitBucketPPRTrigger getBitBucketTrigger(Job<?, ?> job) {
    if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
      ParameterizedJobMixIn.ParameterizedJob<?, ?> pJob =
          (ParameterizedJobMixIn.ParameterizedJob<?, ?>) job;

      List<Trigger<?>> listOfValues = new ArrayList<>(pJob.getTriggers().values());

      for (Trigger<?> trigger : listOfValues) {
        if (trigger instanceof BitBucketPPRTrigger) {
          return (BitBucketPPRTrigger) trigger;
        }
      }
    }

    return null;
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

  private boolean matchMercurialScm(SCM scm, URIish url) {
    try {
      URI hgUri = new URI(((MercurialSCM) scm).getSource());
      String remote = url.toString();
      LOGGER.log(Level.INFO, "Trying to match {0} ", hgUri.toString() + "<-->" + url.toString());      
      if (looselyMatches(hgUri, remote)) {
        LOGGER.info("Machted scm");
        return true;
      }
    } catch (URISyntaxException ex) {
      LOGGER.log(Level.SEVERE, "Could not parse jobSource uri: {0} ", ex);
    }

    return false;
  }

  private boolean matchGitScm(SCM scm, URIish url) {
    for (RemoteConfig remoteConfig : ((GitSCM) scm).getRepositories()) {
      for (URIish urIish : remoteConfig.getURIs()) {
        LOGGER.log(Level.INFO, "Trying to match {0} ", urIish.toString() + "<-->" + url.toString());
        if (GitStatus.looselyMatches(urIish, url)) {
          LOGGER.info("Machted scm");
          return true;
        }
      }
    }

    return false;
  }

  // needed cause the ssh and https URI differs in Bitbucket Server.
  // deprecated
  private URIish parseBitBucketUrIish(URIish urIish) {
    if (urIish.getPath().startsWith("/scm")) {
      urIish = urIish.setPath(urIish.getPath().substring(4));
    }
    return urIish;
  }

  private boolean looselyMatches(URI notifyUri, String repository) {
    boolean result = false;
    try {
      URI repositoryUri = new URI(repository);
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
