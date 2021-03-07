freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestServerCreatedAction("*")
      pullRequestServerUpdatedAction("*")
      pullRequestServerSourceUpdatedAction("*")
      pullRequestServerApprovedAction(false, "*")
      pullRequestServerMergedAction("*")
    }
  }
}
