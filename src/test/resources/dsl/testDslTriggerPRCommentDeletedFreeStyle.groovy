freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentDeletedAction()
    }
  }
}