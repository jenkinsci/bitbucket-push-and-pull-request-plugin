freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestDeclinedAction("**")
    }
  }
}
