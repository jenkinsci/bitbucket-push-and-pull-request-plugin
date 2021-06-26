freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestServerCreatedAction()
      pullRequestServerUpdatedAction()
      pullRequestServerSourceUpdatedAction()
      pullRequestServerApprovedAction(false)
      pullRequestServerMergedAction()
      pullRequestServerDeclinedAction()

      pullRequestServerCreatedAction("*")
      pullRequestServerUpdatedAction("*")
      pullRequestServerSourceUpdatedAction("*")
      pullRequestServerApprovedAction(false, "*")
      pullRequestServerMergedAction("*")
      pullRequestServerDeclinedAction("*")
    }
  }
}
