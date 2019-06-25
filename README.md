# Bitbucket Push and Pull Request Plugin


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

### Environment variables for Bitbucket Cloud ans Server pull requests

| NAME                        | VALUE                  |
|----------------------------:|:-----------------------|
| BITBUCKET_SOURCE_BRANCH     | source branch          |
| BITBUCKET_TARGET_BRANCH     | target branch          |
| BITBUCKET_PULL_REQUEST_LINK | link                   |
| BITBUCKET_PULL_REQUEST_ID   | id                     |
| BITBUCKET_PAYLOAD           | payload as json string | 



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
- Trigger via Pipeline DSL
- Multi-branch pipelines support


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

