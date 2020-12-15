freeStyleJob('test-job1') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction()
    }
  }
}
freeStyleJob('test-job2') {
  triggers {
    bitbucketTriggers {
      repositoryPushAction(false, false, '')
    }
  }
}
