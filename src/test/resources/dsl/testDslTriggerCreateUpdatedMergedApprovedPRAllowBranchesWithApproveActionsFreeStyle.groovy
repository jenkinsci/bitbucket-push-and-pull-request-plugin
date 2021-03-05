freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction("**", true)
      pullRequestUpdatedAction("**", true)
      pullRequestMergedAction("**", true)
      pullRequestApprovedAction(false, "**", true)
    }
  }
}
