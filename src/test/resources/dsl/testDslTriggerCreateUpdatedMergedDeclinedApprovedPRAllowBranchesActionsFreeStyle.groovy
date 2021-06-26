freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction("**")
      pullRequestUpdatedAction("**")
      pullRequestMergedAction("**")
      pullRequestDeclinedAction("**")
      pullRequestApprovedAction(false, "**")
    }
  }
}
