# Bitbucket Push and Pull Request Plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/bitbucket-push-and-pull-request-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/bitbucket-push-and-pull-request-plugin/job/master/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/bitbucket-push-and-pull-request-plugin.svg)](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/graphs/contributors)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/fa86435d30714d0a895222aa9e8e95de)](https://www.codacy.com/manual/cdelmonte-zg/bitbucket-push-and-pull-request-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/bitbucket-push-and-pull-request-plugin&amp;utm_campaign=Badge_Grade)
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

**For infos about the plugin's configuration please visit the webpage** <https://plugins.jenkins.io/bitbucket-push-and-pull-request>


# Environment variables

### Environment variables for Bitbucket Cloud and Server pull requests

| NAME                               | VALUE                  |
|-----------------------------------:|:-----------------------|
| BITBUCKET_SOURCE_BRANCH            | source branch          |
| BITBUCKET_TARGET_BRANCH            | target branch          |
| BITBUCKET_PULL_REQUEST_LINK        | link                   |
| BITBUCKET_PULL_REQUEST_ID          | id                     |
| BITBUCKET_PULL_REQUEST_TITLE       | title                  |
| BITBUCKET_PULL_REQUEST_DESCRIPTION | description            |
| BITBUCKET_ACTOR                    | actor                  |
| BITBUCKET_PAYLOAD                  | payload as json string |



### Environment variables for Bitbucket Cloud  pushs

| NAME                            | VALUE                                    |
|--------------------------------:|:-----------------------------------------|
| REPOSITORY_LINK                 | branch (Deprecated. It will be removed.) |
| BITBUCKET_SOURCE_BRANCH         | branch                                   |
| BITBUCKET_REPOSITORY_URL        | repository url                           |
| BITBUCKET_PAYLOAD               | payload as json string                   |


# Roadmap
- Build Status propagation, cf. #23
- Approve Pull Request on successful build, cf. #29
- Filter source branches for builds triggered through Pull Request, cf. #61


# Dsl Job snippets
```
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
        shell('echo START pull request approved')
    }
}

// repositoryPushAction(boolean triggerAlsoIfTagPush, String allowedBranches)
job('example-push') {
  	triggers{
		bitbucketTriggers {
			repositoryPushAction(false, true, "")
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


# Pipeline script
Example of pipeline code for building on pull-request and push  events.

```
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
