freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestMergedAction("**", true)
    }
  }
}
