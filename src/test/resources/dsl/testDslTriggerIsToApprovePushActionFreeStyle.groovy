freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      repositoryPushAction(false, false, '', true)
    }
  }
}
