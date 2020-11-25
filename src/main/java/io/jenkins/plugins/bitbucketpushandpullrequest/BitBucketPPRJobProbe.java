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

import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_CLOUD_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.PULL_REQUEST_MERGED;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.REPOSITORY_SERVER_PUSH;
import static io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRConstsUtils.PULL_REQUEST_SERVER_MERGED;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.google.common.base.Objects;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import hudson.model.Job;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.triggers.Trigger;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.observer.BitBucketPPRObservable;
import io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRUtils;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;

public class BitBucketPPRJobProbe {
  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRJobProbe.class.getName());
  private BitBucketPPRHookEvent bitbucketEvent;

  {
    System.setErr(BitBucketPPRUtils.createLoggingProxyForErrors(System.err));
  }

  public void triggerMatchingJobs(BitBucketPPRHookEvent bitbucketEvent, BitBucketPPRAction bitbucketAction,
      BitBucketPPRObservable observable) {
    this.bitbucketEvent = bitbucketEvent;

    if (!("git".equals(bitbucketAction.getScm()) || "hg".equals(bitbucketAction.getScm()))) {
      throw new UnsupportedOperationException("Unsupported SCM type " + bitbucketAction.getScm());
    }

    Jenkins.get().getACL();

    try (ACLContext old = ACL.as(ACL.SYSTEM)) {
      List<URIish> remotes = getRemotesAsList(bitbucketAction);
      LOGGER.log(Level.FINE, "Considering remote {0}", remotes);

      Jenkins.get().getAllItems(Job.class).stream().forEach(job -> {
        LOGGER.log(Level.FINE, "Considering candidate job {0}", job.getName());

        triggerScm(job, remotes, bitbucketAction, observable);
      });
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Invalid repository URLs {0}\n{1}",
          new Object[] { bitbucketAction.getScmUrls(), e.getMessage() });
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

  private void triggerScm(Job<?, ?> job, List<URIish> remotes, BitBucketPPRAction bitbucketAction,
      BitBucketPPRObservable observable) {
    LOGGER.log(Level.FINE, "Considering to poke {0}", job.getFullDisplayName());
    BitBucketPPRTrigger bitbucketTrigger = getBitBucketTrigger(job);

    if (bitbucketTrigger != null) {
      List<SCM> scmTriggered = new ArrayList<>();
      Optional<SCMTriggerItem> item = Optional.ofNullable(SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job));

      item.ifPresent(i -> i.getSCMs().stream().forEach(scmTrigger -> {
        LOGGER.log(Level.FINE, "Considering to use trigger {0}", scmTrigger.toString());
        if (isMultiBranchPipeline(job) && mPJobShouldNotBeTriggered(job, bitbucketAction)) {
          LOGGER.log(Level.FINE, "Skipping for job:" + job.getDisplayName());
          return;
        }

        LOGGER.log(Level.FINE, "Scheduling for job:" + job.getDisplayName());

        // TODO: is it needed? There is only a remote, the HTML one, that is used the do
        // the
        // match
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
              new Object[] { bitbucketTrigger.getClass().getName(), job.getFullDisplayName() });

          try {
            bitbucketTrigger.onPost(this.bitbucketEvent, bitbucketAction, scmTrigger, observable);
          } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "Error: {0}", e.getMessage());
            e.printStackTrace();
          }

        } else {
          LOGGER.log(Level.FINE, "{0} SCM doesn't match remote repo {1}", new Object[] { job.getName(), remotes });
        }
      }));
    } else {
      LOGGER.log(Level.FINE, "Bitbucket Trigger for job: {0} is not present. ", job.getFullDisplayName());
    }
  }

  private boolean isMultiBranchPipeline(Job<?, ?> job) {
    LOGGER.log(Level.FINE, "Job is of type: " + job.getParent().getClass().getTypeName());

    return job.getParent() instanceof MultiBranchProject;
  }

  private boolean mPJobShouldNotBeTriggered(Job<?, ?> job, BitBucketPPRAction bitbucketAction) {
    if (job.getDisplayName() != null) {
      String displayName = job.getDisplayName();
      String sourceBranchName = bitbucketAction.getSourceBranch();
      String targetBranchName = bitbucketAction.getTargetBranch();

      LOGGER.log(Level.FINE, "Bitbucket event is : {0}", bitbucketEvent.getAction());
      LOGGER.log(Level.FINE, "Job Name : {0}", displayName);
      LOGGER.log(Level.FINE, "sourceBranchName: {0} ", sourceBranchName);
      LOGGER.log(Level.FINE, "targetBranchName : {0}", targetBranchName);

      if (PULL_REQUEST_MERGED.equalsIgnoreCase(bitbucketEvent.getAction())) {
        return !displayName.equalsIgnoreCase(targetBranchName);
      } else if (PULL_REQUEST_SERVER_MERGED.equalsIgnoreCase(bitbucketEvent.getAction())) {
        return !displayName.equalsIgnoreCase(targetBranchName);
      } else if (sourceBranchName != null) {
        return !displayName.equalsIgnoreCase(sourceBranchName);
      } else if (REPOSITORY_CLOUD_PUSH.equalsIgnoreCase(bitbucketEvent.getAction()) && targetBranchName != null) {
        return !displayName.equals(targetBranchName);
      } else if (REPOSITORY_SERVER_PUSH.equalsIgnoreCase(bitbucketEvent.getAction()) && targetBranchName != null) {
        return !displayName.equals(targetBranchName);
      }
    }

    return true;
  }

  private BitBucketPPRTrigger getBitBucketTrigger(Job<?, ?> job) {
    BitBucketPPRTrigger trigger = null;

    if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
      ParameterizedJobMixIn.ParameterizedJob<?, ?> pJob = (ParameterizedJobMixIn.ParameterizedJob<?, ?>) job;

      for (Trigger<?> t : pJob.getTriggers().values()) {
        if (t instanceof BitBucketPPRTrigger) {
          trigger = (BitBucketPPRTrigger) t;
        }
      }
    }
    return trigger;
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
    boolean result = false;

    try {
      URI hgUri = new URI(((MercurialSCM) scm).getSource());

      LOGGER.log(Level.INFO, "Trying to match {0} ", hgUri.toString() + "<-->" + remote.toString());
      result = hgLooselyMatches(hgUri, remote.toString());

      if (result) {
        LOGGER.info("Matched scm ");
      } else {
        LOGGER.info(() -> "Didn't match scm " + hgUri.toString());
      }
    } catch (URISyntaxException ex) {
      LOGGER.log(Level.SEVERE, "Could not parse jobSource uri: {0} ", ex);
    }

    return result;
  }

  private boolean matchGitScm(SCM scm, URIish remote) {
    GitSCM gitSCM = (GitSCM) scm;

    for (RemoteConfig remoteConfig : gitSCM.getRepositories()) {
      for (URIish urIish : remoteConfig.getURIs()) {
        LOGGER.log(Level.INFO, "Trying to match {0} ", urIish.toString() + "<-->" + remote.toString());

        // TODO: Should we implement the method or continue using the GitStatus one?
        if (GitStatus.looselyMatches(urIish, remote)) {
          LOGGER.info("Matched scm");
          return true;
        }
      }
    }

    return false;
  }

  private boolean hgLooselyMatches(URI notifyUri, String repository) {
    boolean result = false;
    try {
      if (!hgIsUnexpandedEnvVar(repository)) {
        URI repositoryUri = new URI(repository);
        LOGGER.log(Level.INFO, "Mercurial loose match between {0} ",
            notifyUri.toString() + "<- and ->" + repositoryUri.toString());
        result = Objects.equal(notifyUri.getHost(), repositoryUri.getHost())
            && Objects.equal(StringUtils.stripEnd(notifyUri.getPath(), "/"),
                StringUtils.stripEnd(repositoryUri.getPath(), "/"))
            && Objects.equal(notifyUri.getQuery(), repositoryUri.getQuery());
      }
    } catch (URISyntaxException ex) {
      LOGGER.log(Level.SEVERE, "could not parse repository uri " + repository, ex);
    }
    return result;
  }

  private boolean hgIsUnexpandedEnvVar(String str) {
    return str.startsWith("$");
  }

  public boolean testMatchMercurialScm(SCM scm, URIish remote) {
    return matchMercurialScm(scm, remote);
  }

  void onCompleted(BitBucketPPRTriggerCause cause, Result result, String buildUrl) {
    if (cause == null) {
      return;
    }
  }
}
