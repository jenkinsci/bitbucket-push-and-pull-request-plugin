# Bitbucket Push and Pull Request Plugin


Plugin for Jenkins **v2.138.2 or later**, that trigger builds on Bitbucket's push and pull requests.
It's based on the Sazo's fork (<https://github.com/sazo/bitbucket-plugin>)
of the Bitbucket plugin: <https://plugins.jenkins.io/bitbucket>.

The new features introduced by Bitbucket Push and Pull Request are:
- improved support of pushs for Bitbucket cloud (rest api v2.x+) and Bitbucket server (5.14+)
- support of pull requests for Bitbucket cloud (rest api v2.x+) (thanks Sazo)
- usage of Gson instead of net.sf.json.JSONObject (blacklisted starting from Jenkins 2.102+)
- Introduction of Models and security improvements

Bitbucket Push and Pull Request supports the
- Bitbucket cloud rest api v2.x+ and later
- Bitbucket server 5.14+ and later

**Before you start...**
Bitbucket Push And Pull Request Plugin will not work if the old Bitbucket plugin <https://plugins.jenkins.io/bitbucket> is still installed. So, please de-install from Jenkins the previous Bitbucket plugin if you want to use this new one.

**For infos about the plugin's configuration please visit the webpage** <https://plugins.jenkins.io/bitbucket-push-and-pull-request>

# Roadmap
- introduce pullrequests for Bitbucket server 5.14+ and later
- improve DSL pipelines scripting


# Dsl Job snippets
```
job('example-pull-request-created') {
  	triggers{
  		bitbucketPullRequestCreatedAction()
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

job('example-pull-request-updated') {
  	triggers{
  		bitbucketPullRequestUpdatedAction()
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

// bitbucketPullRequestApprovedAction(boolean onlyIfReviewersApproved)
job('example-pull-request-approved') {
  	triggers{
  		bitbucketPullRequestApprovedAction(false)
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

// bitbucketRepositoryPushAction(boolean triggerAlsoIfTagPush, String allowedBranches)
job('example-push') {
  	triggers{
  		bitbucketRepositoryPushAction(false, "")
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
```


# Pipeline script
Example of pipeline code for building on pull-request create event. It merge from source to target in the PR.

```
properties([
    pipelineTriggers([
        [
            $class: 'BitBucketPPRTrigger',
            triggers : [
                [
                    $class: 'BitBucketPPRPullRequestTriggerFilter',
                    actionFilter: [
                        $class: 'BitBucketPPRPullRequestCreatedActionFilter'
                    ]
                ]
            ]
        ]
    ])
])
node {
        def sourceBranch = ""
        def targetBranch = ""
        try{
            sourceBranch = "${BITBUCKET_SOURCE_BRANCH}";
            targetBranch = "${BITBUCKET_TARGET_BRANCH}";
        }catch(e){}

        if(sourceBranch == ""){
            sourceBranch = 'development'
        }

        if(targetBranch == ""){
            targetBranch = 'master'
        }

        checkout changelog: true, poll: true, scm: [
            $class: 'GitSCM',
            branches: [
                [name: '*/'+sourceBranch]
            ],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                 [
                    $class: 'PreBuildMerge',
                    options: [
                        fastForwardMode: 'FF',
                        mergeRemote: 'origin',
                        mergeStrategy: 'recursive',
                        mergeTarget: ''+targetBranch
                    ]
                ]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                [
                    url: 'https://[user]@bitbucket.org/[org]/[repo].git']
                ]
            ]


        echo 'Some build steps'

}
```
