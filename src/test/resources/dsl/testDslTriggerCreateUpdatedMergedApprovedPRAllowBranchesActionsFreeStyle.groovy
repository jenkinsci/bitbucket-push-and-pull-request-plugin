freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction("**")
      pullRequestUpdatedAction("**")
      pullRequestMergedAction("**")
      pullRequestApprovedAction(false, "**")
    }
  }
}