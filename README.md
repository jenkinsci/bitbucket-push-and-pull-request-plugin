
# bitbucket-push-and-pull-request

This plugin is based on the Sazo's fork (<https://github.com/sazo/bitbucket-plugin>)
of the official Bitbucket Plugin (<https://plugins.jenkins.io/bitbucket>).

Apart of a lot of refactoring, the major changes are:
- the usage of Gson instead of net.sf.json.JSONObject (from Jenkins 2.102+ JSONObject is blacklisted)
- Introduction of Models and security improvements


This plugin supports the bitbucket rest api v2.x+ and requires a Jenkins 2.102+ 


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
