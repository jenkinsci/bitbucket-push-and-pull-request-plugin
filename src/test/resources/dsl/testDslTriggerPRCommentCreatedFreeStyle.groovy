freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentCreatedAction()
    }
  }
}