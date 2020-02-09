# Bitbucket Push and Pull Request Plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/bitbucket-push-and-pull-request-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/bitbucket-push-and-pull-request-plugin/job/master/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/bitbucket-push-and-pull-request-plugin.svg)](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/graphs/contributors)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/fa86435d30714d0a895222aa9e8e95de)](https://www.codacy.com/manual/cdelmonte-zg/bitbucket-push-and-pull-request-plugin?utm_source=github.com&utm_medium=referral&utm_content=jenkinsci/bitbucket-push-and-pull-request-plugin&utm_campaign=Badge_Grade)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/bitbucket-push-and-pull-request.svg)](https://plugins.jenkins.io/bitbucket-push-and-pull-request)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/bitbucket-push-and-pull-request-plugin.svg?label=changelog)](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/bitbucket-push-and-pull-request.svg?color=blue)](https://plugins.jenkins.io/bitbucket-push-and-pull-request)
[![Gitter](https://badges.gitter.im/jenkinsci/bitbucket-push-and-pull-request-plugin.svg)](https://gitter.im/jenkinsci/bitbucket-push-and-pull-request-plugin)

<img src="docs/img/logo-head.svg" width="192">

Plugin for Jenkins **v2.138.2 or later**, that trigger builds on Bitbucket's push and pull requests.
It is based on the Sazo's fork (<https://github.com/sazo/bitbucket-plugin>)
of the Bitbucket plugin: <https://plugins.jenkins.io/bitbucket>.

The new features introduced by Bitbucket Push and Pull Request 2.x.x are:

- improved support of pushs for Bitbucket cloud (rest api v2.x+ with mercurial and git) and Bitbucket server (5.14+ with git)
- support of pull requests for Bitbucket cloud (rest api v2.x+ with mercurial and git) and bitbucket Server (5.14+ with git)
- usage of Gson instead of net.sf.json.JSONObject (blacklisted starting from Jenkins 2.102+)
- Introduction of Models and security improvements

Bitbucket Push and Pull Request supports the

- Bitbucket cloud rest api v2.x+ and later (with git and mercurial repos)
- Bitbucket server 5.14+ and later (with git repos)

**Before you start...**
Bitbucket Push And Pull Request Plugin will not work if the old Bitbucket plugin <https://plugins.jenkins.io/bitbucket> is still installed. So, please de-install from Jenkins the previous Bitbucket plugin if you want to use this new one.

**... and a warning:** After updating the plugin from a version prior to the 2.x.x, the jobs with a pull request need to be reconfigured, reselecting once again, from the plugin conf. pane, the pull request event, that will trigger the build.

## Configuration

### Before you start

Bitbucket Push And Pull Request Plugin will not work if the Bitbucket plugin (<https://plugins.jenkins.io/bitbucket>) is still installed. So, please de-install the previous Bitbucket plugin if you want to use this new one.

### Configure the webhook

In case you are using Bitbucket Cloud, configure your Bitbucket repository adding a webhook in the settings page. In the URL field (see image, at point A) add your JENKINS_URL followed by "/bitbucket-hook/" (for example <https://my-jenkins.on-my-planet-far-away.com/bitbucket-hook/>) Credentials for the webhook endpoint are not required, the trailing slash is mandatory. For more infos please consult the resource <https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html>.

If you are using Bitbucket Server, follow these instructions: <https://confluence.atlassian.com/bitbucketserver/managing-webhooks-in-bitbucket-server-938025878.html>.

### Configure your Jenkins job

1. Configure the Bitbucket Repository under the Source Code Management with your credentials. For git:

   <img src="docs/img/conf_git.png" width="580">

   In case you are using Mercurial instead of git, configure it as follows:

   <img src="docs/img/conf_mercurial.png" width="580">

   _Please note: the branch, related to the events which trigger the builds, must be specified in the field Revision._

2. Now activate the plugin in your job selecting the "Build with Bitbucket Push and Pull Request Plugin" option in the Build Triggers pane.

   <img src="docs/img/example_config_jenkins_bb_ppr_1.png" width="580">

   <img src="docs/img/example_config_jenkins_bb_ppr_2.png" width="580">

   <img src="docs/img/example_config_jenkins_bb_ppr_3.png" width="580">

   <img src="docs/img/example_config_jenkins_bb_ppr_4.png" width="580">

## Environment variables

### Environment variables for Bitbucket Cloud ans Server pull requests

|                        NAME | VALUE                  |
| --------------------------: | :--------------------- |
|     BITBUCKET_SOURCE_BRANCH | source branch          |
|     BITBUCKET_TARGET_BRANCH | target branch          |
| BITBUCKET_PULL_REQUEST_LINK | link                   |
|   BITBUCKET_PULL_REQUEST_ID | id                     |
|           BITBUCKET_PAYLOAD | payload as json string |

### Environment variables for Bitbucket Cloud pushs

|                     NAME | VALUE                                    |
| -----------------------: | :--------------------------------------- |
|          REPOSITORY_LINK | branch (Deprecated. It will be removed.) |
|  BITBUCKET_SOURCE_BRANCH | branch                                   |
| BITBUCKET_REPOSITORY_URL | repository url                           |
|        BITBUCKET_PAYLOAD | payload as json string                   |

## Roadmap

- Build Status propagation, cf. #23
- Approve Pull Request on successful build, cf. #29

## Dsl Job actions for Bitbucket Push and Pull Request Trigger

```groovy
bitbucketTriggers {
  
  // For Bitbucket Cloud
  repositoryPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String)
  pullRequestApprovedAction(onlyIfReviewersApproved: boolean, allowedBranches: String)
  pullRequestApprovedAction(onlyIfReviewersApproved: boolean)
  pullRequestCreatedAction()
  pullRequestCreatedAction(allowedBranches: String)
  pullRequestUpdatedAction()
  pullRequestUpdatedAction(allowedBranches: String)
  pullRequestMergedAction()
  pullRequestMergedAction(allowedBranches: String)
  
  // For Bitbucket Server
  repositoryServerPushAction(triggerAlsoIfTagPush: boolean, triggerAlsoIfNothingChanged: boolean, allowedBranches: String)
  pullRequestServerApprovedAction(onlyIfReviewersApproved: boolean)
  pullRequestServerApprovedAction(onlyIfReviewersApproved: boolean, allowedBranches: String)
  pullRequestServerCreatedAction()
  pullRequestServerCreatedAction(allowedBranches: String)
  pullRequestServerUpdatedAction()
  pullRequestServerUpdatedAction(allowedBranches: String)
  pullRequestServerMergedAction()
  pullRequestServerMergedAction(allowedBranches: String)
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
            allowedBranches: ""
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

## Sponsored By

<img src="docs/img/silpion_logo.png">
