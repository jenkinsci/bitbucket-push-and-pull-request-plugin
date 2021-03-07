freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestApprovedAction(false, "**")
    }
  }
}
