freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction()
    }
  }
}