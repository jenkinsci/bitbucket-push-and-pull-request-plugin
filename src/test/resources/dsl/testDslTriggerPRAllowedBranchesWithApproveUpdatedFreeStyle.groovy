freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestUpdatedAction("**", true)
    }
  }
}
