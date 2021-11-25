freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestServerCreatedAction()
      pullRequestServerUpdatedAction()
      pullRequestServerApprovedAction(false)
      pullRequestServerMergedAction()
      pullRequestServerDeclinedAction()
    }
  }
}
