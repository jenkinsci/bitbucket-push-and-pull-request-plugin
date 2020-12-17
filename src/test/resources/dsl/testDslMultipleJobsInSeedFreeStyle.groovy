/*
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
*/
freeStyleJob('test-job1') {
  properties {
    pipelineTriggers {
      triggers {
        bitBucketTrigger {
          triggers {
            bitBucketPPRPullRequestTriggerFilter  {
              actionFilter {
                bitBucketPPRPullRequestCreatedActionFilter {
                }
              }
            }
          }
        }
      }
    }
  }
}
freeStyleJob('test-job2') {
  properties {
    pipelineTriggers {
      triggers {
        bitBucketTrigger {
          triggers {
            bitBucketPPRRepositoryTriggerFilter {
              actionFilter {
                bitBucketPPRRepositoryPushActionFilter {
                  triggerAlsoIfTagPush(false)
                  triggerAlsoIfNothingChanged(false)
                  allowedBranches('')
                }
              }
            }
          }
        }
      }
    }
  }
}