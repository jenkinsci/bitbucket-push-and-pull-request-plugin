# Bitbucket Push and Pull Request Plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/bitbucket-push-and-pull-request-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/bitbucket-push-and-pull-request-plugin/job/master/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/bitbucket-push-and-pull-request-plugin.svg)](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/graphs/contributors)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/fa86435d30714d0a895222aa9e8e95de)](https://www.codacy.com/manual/cdelmonte-zg/bitbucket-push-and-pull-request-plugin?utm_source=github.com&utm_medium=referral&utm_content=jenkinsci/bitbucket-push-and-pull-request-plugin&utm_campaign=Badge_Grade)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/bitbucket-push-and-pull-request.svg)](https://plugins.jenkins.io/bitbucket-push-and-pull-request)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/bitbucket-push-and-pull-request-plugin.svg?label=changelog)](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/bitbucket-push-and-pull-request.svg?color=blue)](https://plugins.jenkins.io/bitbucket-push-and-pull-request)
[![Gitter](https://badges.gitter.im/jenkinsci/bitbucket-push-and-pull-request-plugin.svg)](https://gitter.im/jenkinsci/bitbucket-push-and-pull-request-plugin)

<img src="img/logo-head.svg" width="192">

Plugin for Jenkins **v2.138.2 or later**, that triggers builds on Bitbucket's push and pull requests events.

The new features introduced by Bitbucket Push and Pull Request 2.x.x are:

- build state propagation (only for bb cloud)
- support of pull requests for Bitbucket cloud (rest api v2.x+ with mercurial and git) and bitbucket Server (5.14+ with git)
- support of pushs for Bitbucket cloud (rest api v2.x+ with mercurial and git) and Bitbucket server (5.14+ with git)
- usage of Gson instead of net.sf.json.JSONObject
- Introduction of Models and major security improvements

Bitbucket Push and Pull Request supports the

- Bitbucket cloud rest api v2.x+ and later (with git and mercurial repos)
- Bitbucket server 5.14+ and later (with git repos)

**Before you start...**
Bitbucket Push And Pull Request Plugin will not work if the old Bitbucket plugin <https://plugins.jenkins.io/bitbucket> is still installed. So, please de-install from Jenkins the previous Bitbucket plugin if you want to use this new one.

**... and a warning:** After updating the plugin from a version prior to 2.0.0, the jobs with a pull request need to be reconfigured, reselecting once again, from the plugin conf. pane, the pull request event, that will trigger the build.

## Some important aspects to keep in mind

### About the initialization of the plugin

The Bitbucket Push and Pull Request Plugin (**BBPPR Plugin**) uses the **Git Plugin** to handle with the git repositories managed by BitBucket. More in details:

1. The user initiliazes a new job starting manually a build.
2. The **Git Plugin** clones the repository declared in the job fetching the branches as defined in the **Branch Specifier** filter field.
3. Due to the fact that each branch is new, the **BBPPR Plugin** *sees* everywhere changes and, as a consequence, the **BBPPR Plugin** starts for each new fetched branch a job.
4. It's also important to keep in mind that the filters on the branches specified by the **BBPPR Plugin** in its **Allowed Branches** field, are not applied in the initialization phase, because they are only active for jobs triggered through the BitBucket webhook (and not for the ones, which are built manually).

### About the filters on the branches

Talking generally, there are two filters used to 'control the branches':

- the **Git plugin** filter on the branches: a kind of general filter applied at job level.
- the **BBPPR plugin** filter: a specific one, that acts at trigger level and that is there because the user can have in a job more triggers than only one.

- Important is to keep in mind, that you cannot trigger events on branches that are excluded from the Git Plugin filter. It has the precedence because it defines (fetches) the branches, that can be accessed by the **BBPPR plugin**.

- Moreover, the way in which the matching is done is the same for both plugins, because **we are using for the matching the public method given to disposal from the Git Plugin**.
The reason for that is the wish to guarantee consistency between the two plugins, at least regarding the way in which branches are matched. This all means also, that we are not implementing the matching logic.

- As you see from the code: `return matches(allowedBranches, bitbucketAction.getTargetBranch(), null);`, the matching is between the string you specified (`allowedBranches`) and the **target branch** specified in the payload sent from BitBucket (`bitbucketAction.getTargetBranch()`). This means that **The filter is not on the source branch, but on the target branch.**

- You can find al lot of examples in this test class: [BitBucketPPRPullRequestCreatedActionFilterTest.java](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/blob/master/src/test/java/io/jenkins/plugins/bitbucketpushandpullrequest/filter/pullrequest/cloud/BitBucketPPRPullRequestCreatedActionFilterTest.java)

### About the build state propagation and the approve set by Jenkins on BitBucket

- This implementation works actually **only for BitBucket Cloud**. The implementation for the BitBucket Server follows in the release 2.5.0

- Actually, the bitbucket credentials needed for the state propagation are the ones provided in the git plugin configuration for the job. That means, that you don't need to give other credentials: **it should work out of the box**.

The only limit of this solution is that **the build status propagation will work only if you are using**, for the git plugin, **credentials of the kind: "Username with Password"**.

## Configuration

### Before you start

Bitbucket Push And Pull Request Plugin will not work if the Bitbucket plugin (<https://plugins.jenkins.io/bitbucket>) is still installed. So, please de-install the previous Bitbucket plugin if you want to use this new one.

### Configure the webhook

Configure your Bitbucket repository adding a webhook in the settings page. In the URL field (see image, at point A) add your JENKINS_URL followed by "/bitbucket-hook/" (for example <https://my-jenkins.on-my-planet-far-away.com/bitbucket-hook/>) Credentials for the webhook endpoint are not required, the trailing slash is mandatory.

For more specific infos about managing webhooks please consult:

- <https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html>. (Bitbucket Cloud)
- <https://confluence.atlassian.com/bitbucketserver/managing-webhooks-in-bitbucket-server-938025878.html>. (Bitbucket Server)

### Configure your Jenkins job

1. Configure the Bitbucket Repository under the Source Code Management with your credentials. For git:
![example config git](img/conf_git.png)
In case you are using Mercurial instead of git, configure it as follows:
![exampel config mercurial](img/conf_mercurial.png)
_Please note: the branch, related to the events which trigger the builds, must be specified in the field Revision._

2. Now activate the plugin in your job selecting the "Build with Bitbucket Push and Pull Request Plugin" option in the Build Triggers pane.
![example config jenkins bb ppr 1](img/example_config_jenkins_bb_ppr_1.png)
![example config jenkins bb ppr 2](img/example_config_jenkins_bb_ppr_2.png)
![example config jenkins bb ppr 3](img/example_config_jenkins_bb_ppr_3.png)
![example config jenkins bb ppr 4](img/example_config_jenkins_bb_ppr_4.png)

## Environment variables

### Environment variables for Bitbucket Cloud ans Server pull requests

|                               NAME | VALUE                  |
| ---------------------------------: | :--------------------- |
|            BITBUCKET_SOURCE_BRANCH | source branch          |
|            BITBUCKET_TARGET_BRANCH | target branch          |
|        BITBUCKET_PULL_REQUEST_LINK | link                   |
|          BITBUCKET_PULL_REQUEST_ID | id                     |
|                    BITBUCKET_ACTOR | actor name             |
|       BITBUCKET_PULL_REQUEST_TITLE | PR title               |
| BITBUCKET_PULL_REQUEST_DESCRIPTION | PR description         |
|                  BITBUCKET_PAYLOAD | payload as json string |

### Environment variables for Bitbucket Cloud pushs

|                      NAME | VALUE                                      |
| ------------------------: | :----------------------------------------  |
|           REPOSITORY_LINK | branch (Deprecated. It will be removed.)   |
|   BITBUCKET_SOURCE_BRANCH | branch                                     |
|   BITBUCKET_TARGET_BRANCH | branch                                     |
|  BITBUCKET_REPOSITORY_URL | repository url                             |
| BITBUCKET_REPOSITORY_UUID | repository uuid                            |
|   BITBUCKET_REPOSITORY_ID | repository uuid (for compat. with server)  |
|           BITBUCKET_ACTOR | actor name                                 |
|         BITBUCKET_PAYLOAD | payload as json string                     |

### Environment variables for Bitbucket Server pushs

|                      NAME | VALUE                                      |
| ------------------------: | :----------------------------------------  |
|   BITBUCKET_SOURCE_BRANCH | branch                                     |
|   BITBUCKET_TARGET_BRANCH | branch                                     |
|           REPOSITORY_NAME | repository name                            |
|   BITBUCKET_REPOSITORY_ID | repository id                              |
|           BITBUCKET_ACTOR | actor name                                 |
|         BITBUCKET_PAYLOAD | payload as json string                     |

## Roadmap version 2.5.0

- Build Status propagation for BB Server
- Approve Pull Request and Push on successful build for for BB Server

## Dsl Job actions for Bitbucket Push and Pull Request Trigger

```groovy
bitbucketTriggers {

  // For Bitbucket Cloud
  repositoryPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String)
  repositoryPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String, isToApprove: boolean)
  repositoryPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String, isToApprove: boolean, triggerOnlyIfTagPush: boolean)

  pullRequestApprovedAction(onlyIfReviewersApproved: boolean)
  pullRequestApprovedAction(onlyIfReviewersApproved: boolean, allowedBranches: String)
  pullRequestApprovedAction(onlyIfReviewersApproved: boolean, allowedBranches: String, isToApprove: boolean)

  pullRequestCreatedAction()
  pullRequestCreatedAction(allowedBranches: String)
  pullRequestCreatedAction(allowedBranches: String, isToApprove: String)

  pullRequestUpdatedAction()
  pullRequestUpdatedAction(allowedBranches: String)
  pullRequestUpdatedAction(allowedBranches: String, isToApprove: String)

  pullRequestMergedAction()
  pullRequestMergedAction(allowedBranches: String)
  pullRequestMergedAction(allowedBranches: String, isToApprove: String)

  pullRequestCommentCreatedAction()
  pullRequestCommentCreatedAction(allowedBranches: String)
  pullRequestCommentCreatedAction(allowedBranches: String, commentFilter: String) // CommentFilter is java a regex expression

  pullRequestCommentUpdatedAction()
  pullRequestCommentUpdatedAction(allowedBranches: String)
  pullRequestCommentUpdatedAction(allowedBranches: String, commentFilter: String) // CommentFilter is java a regex expression

  pullRequestCommentDeletedAction()
  pullRequestCommentDeletedAction(allowedBranches: String)

  // For Bitbucket Server
  // note: flag `isToApprove` has no effect yet
  repositoryServerPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String)
  repositoryServerPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String, isToApprove: boolean)
  repositoryServerPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String, isToApprove: boolean, triggerOnlyIfTagPush: boolean)


  pullRequestServerApprovedAction(onlyIfReviewersApproved: boolean)
  pullRequestServerApprovedAction(onlyIfReviewersApproved: boolean, allowedBranches: String)
  pullRequestServerApprovedAction(onlyIfReviewersApproved: boolean, allowedBranches: String, isToApprove: boolean)

  pullRequestServerCreatedAction()
  pullRequestServerCreatedAction(allowedBranches: String)
  pullRequestServerCreatedAction(allowedBranches: String, isToApprove: boolean)

  pullRequestServerUpdatedAction()
  pullRequestServerUpdatedAction(allowedBranches: String)
  pullRequestServerUpdatedAction(allowedBranches: String, isToApprove: boolean)

  pullRequestServerMergedAction()
  pullRequestServerMergedAction(allowedBranches: String)
  pullRequestServerMergedAction(allowedBranches: String, isToApprove: boolean)
}
```

## Dsl Job snippets

```groovy
// pullRequestCreatedAction()
job('example-pull-request-created') {
  triggers{
    bitbucketTriggers {
      pullRequestCreatedAction()
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request created')
  }
}

// pullRequestCreatedAction() with filter on branches
job('example-pull-request-created-with-filter-on-branches') {
  triggers{
    bitbucketTriggers {
      pullRequestCreatedAction("master")
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request created with filter on branches')
  }
}

// pullRequestCreatedAction() with filter on branches
// and eventually approve Pull Request in BB after the job is done
job('example-pull-request-created-with-filter-on-branches-and-is-to-approve') {
  triggers{
    bitbucketTriggers {
      pullRequestCreatedAction("master", true)
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request created with filter on branches')
  }
}

// pullRequestUpdatedAction()
job('example-pull-request-updated') {
  triggers{
    bitbucketTriggers {
      pullRequestUpdatedAction()
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request updated')
  }
}

// pullRequestUpdatedAction() wiht filter on branches
job('example-pull-request-updated-with-filter-on-branches') {
  triggers{
    bitbucketTriggers {
      pullRequestUpdatedAction("master")
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request updated with filter on branches')
  }
}

// pullRequestUpdatedAction() wiht filter on branches
// and eventually approve Pull Request in BB after the job is done
job('example-pull-request-updated-with-filter-on-branches') {
  triggers{
    bitbucketTriggers {
      pullRequestUpdatedAction("master", true)
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request updated with filter on branches')
  }


// pullRequestApprovedAction(boolean onlyIfReviewersApproved)
job('example-pull-request-approved') {
  triggers{
    bitbucketTriggers {
      pullRequestApprovedAction(false)
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request approved')
  }
}

// pullRequestApprovedAction(boolean onlyIfReviewersApproved) with filter on branches
job('example-pull-request-approved-with-filter-on-branches') {
  triggers{
    bitbucketTriggers {
      pullRequestApprovedAction(false, "master")
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request approved with filter on branches')
  }
}


// pullRequestApprovedAction(boolean onlyIfReviewersApproved) with filter on branches
// and eventually approve Pull Request in BB after the job is done
job('example-pull-request-approved-with-filter-on-branches') {
  triggers{
    bitbucketTriggers {
      pullRequestApprovedAction(false, "master", true)
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request approved with filter on branches')
  }
}

// pullRequestMergedAction()
job('example-pull-request-merged') {
  triggers{
    bitbucketTriggers {
      pullRequestMergedAction()
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request merged')
  }
}

// pullRequestMergedAction() with filter on branches
job('example-pull-request-merged-with-filter-on-branches') {
  triggers{
    bitbucketTriggers {
      pullRequestMergedAction("master")
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request merged with filter on branches')
  }
}

// pullRequestMergedAction() with filter on branches
// and eventually approve Pull Request in BB after the job is done
job('example-pull-request-merged-with-filter-on-branches') {
  triggers{
    bitbucketTriggers {
      pullRequestMergedAction("master")
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
    shell('echo START pull request merged with filter on branches')
  }
}

// repositoryPushAction(boolean triggerAlsoIfTagPush, boolean triggerAlsoIfNothingChanged, String allowedBranches)
job('example-push') {
  triggers{
    bitbucketTriggers {
      repositoryPushAction(false, true, "master")
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
      shell('echo START push')
  }
}


// repositoryPushAction(boolean triggerAlsoIfTagPush, boolean triggerAlsoIfNothingChanged, String allowedBranches)
// and eventually approve Pull Request in BB after the job is done
job('example-push') {
  triggers{
    bitbucketTriggers {// and eventually approve Pull Request in BB after the job is done

      repositoryPushAction(false, true, "master", true)
    }
  }
  scm {
    git {
      remote {
        url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
      shell('echo START push')
  }
}

// combination of triggers is also possible
job('example-pull-request-created-updated') {
  triggers{
    bitbucketTriggers {
      pullRequestCreatedAction()
      pullRequestUpdatedAction()
      pullRequestMergedAction("master")
    }
  }
  scm {
    git {
      remote {
          url("https://git.company.domain/scm/~username/telegram.git")
      }
    }
  }
  steps {
      shell('echo START pull request created')
  }
}
```

## Pipeline script

Example of pipeline code for building on pull-request and push events.

```groovy
properties([
  pipelineTriggers([
    [
      $class: 'BitBucketPPRTrigger',
      triggers : [
        [
          $class: 'BitBucketPPRPullRequestTriggerFilter',
          actionFilter: [
            $class: 'BitBucketPPRPullRequestCreatedActionFilter',
          ]
        ],
        [
          $class: 'BitBucketPPRPullRequestTriggerFilter',
          actionFilter: [
            $class: 'BitBucketPPRPullRequestApprovedActionFilter',
          ]
        ],
        [
          $class: 'BitBucketPPRPullRequestTriggerFilter',
          actionFilter: [
            $class: 'BitBucketPPRPullRequestUpdatedActionFilter',
          ]
        ],
        [
          $class: 'BitBucketPPRPullRequestTriggerFilter',
          actionFilter: [
            $class: 'BitBucketPPRPullRequestMergedActionFilter',
          ]
        ],
        [
          $class: 'BitBucketPPRRepositoryTriggerFilter',
          actionFilter: [
            $class: 'BitBucketPPRRepositoryPushActionFilter',
            triggerAlsoIfNothingChanged: true,
            triggerAlsoIfTagPush: false,
            allowedBranches: "",
            isToApprove: true
          ]
        ]
      ]
    ]
  ])
])

pipeline {
  agent any

  stages {
  stage('Build') {
      steps {
        echo 'Building...'

        echo 'Env vars for cloud pull request...'
        echo "BITBUCKET_SOURCE_BRANCH ${env.BITBUCKET_SOURCE_BRANCH}"
        echo "BITBUCKET_TARGET_BRANCH ${env.BITBUCKET_TARGET_BRANCH}"
        echo "BITBUCKET_PULL_REQUEST_LINK ${env.BITBUCKET_PULL_REQUEST_LINK}"
        echo "BITBUCKET_PULL_REQUEST_ID ${env.BITBUCKET_PULL_REQUEST_ID}"
        echo "BITBUCKET_PAYLOAD ${env.BITBUCKET_PAYLOAD}"

        echo 'Env vars for cloud push...'
        echo "REPOSITORY_LINK ${env.REPOSITORY_LINK}"
        echo "BITBUCKET_SOURCE_BRANCH ${env.BITBUCKET_SOURCE_BRANCH}"
        echo "BITBUCKET_REPOSITORY_URL ${env.BITBUCKET_REPOSITORY_URL}"
        echo "BITBUCKET_PUSH_REPOSITORY_UUID ${env.BITBUCKET_PUSH_REPOSITORY_UUID}"
        echo "BITBUCKET_PAYLOAD ${env.BITBUCKET_PAYLOAD}"

        echo 'Env vars for server push...'
        echo "REPOSITORY_LINK ${env.REPOSITORY_LINK}"
        echo "BITBUCKET_SOURCE_BRANCH ${env.BITBUCKET_SOURCE_BRANCH}"
        echo "BITBUCKET_REPOSITORY_URL ${env.BITBUCKET_REPOSITORY_URL}"
        echo "BITBUCKET_PUSH_REPOSITORY_UUID ${env.BITBUCKET_PUSH_REPOSITORY_UUID}"
        echo "BITBUCKET_PAYLOAD ${env.BITBUCKET_PAYLOAD}"
      }
    }
  }
}
```

## Thanks and Acknowledgments

This plugin is originally based on the Sazo's fork (<https://github.com/sazo/bitbucket-plugin>)
of the Bitbucket plugin: <https://plugins.jenkins.io/bitbucket>.

## Sponsored By

![Silpion](./img/silpion_logo.png)
