/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      repositoryPushAction(false, false, '')
    }
  }
}
*/
freeStyleJob('test-job') {
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
