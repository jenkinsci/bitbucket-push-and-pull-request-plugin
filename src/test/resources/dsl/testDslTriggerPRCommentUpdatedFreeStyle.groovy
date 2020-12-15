freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentUpdatedAction()
    }
  }
}