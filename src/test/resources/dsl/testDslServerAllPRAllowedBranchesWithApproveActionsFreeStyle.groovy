freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestServerCreatedAction("*", true)
      pullRequestServerUpdatedAction("*", true)
      pullRequestServerSourceUpdatedAction("*", true)
      pullRequestServerApprovedAction(false, "*", true)
      pullRequestServerMergedAction("*", true)
    }
  }
}