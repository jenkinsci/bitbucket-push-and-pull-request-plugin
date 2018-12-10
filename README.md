
# bitbucket-push-and-pull-request

This plugin is based on the Sazo's fork (<https://github.com/sazo/bitbucket-plugin>)
of the official Bitbucket Plugin (<https://plugins.jenkins.io/bitbucket>).

The new features introduced by bitbucket-push-and-pull-request are:
- support of pull requests (thanks Sazo)
- usage of Gson instead of net.sf.json.JSONObject (blacklisted starting from Jenkins 2.102+)
- Introduction of Models and security improvements

bitbucket-push-and-pull-request supports the bitbucket rest api v2.x+

# Why another BitBucket plugin?

The main reasons why we should have IMHO a new bitbucket plugin are not only related to the missing pull request functionality.
In particular:

- The official bitbucket plugin uses JSONObject, which was blacklisted by the JEP-200, causing now an exception  UnsupportedOperationException: Refusing to marshal net.sf.json.JSONObject for security reasons (see:  <https://github.com/jenkinsci/bitbucket-plugin/search?q=JSONObject&unscoped_q=JSONObject> )         
  
- Furthermore, the official bitbucket plugin is missing of any kind of development at this time, as you can see reading the comments on the wiki page: <https://wiki.jenkins.io/display/JENKINS/BitBucket+Plugin>


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
