/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      repositoryPushAction(false, false, '', true)
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
                bitBucketPPRRepositoryPushActionFilter  {
                  triggerAlsoIfTagPush(false)
                  triggerAlsoIfNothingChanged(false)
                  allowedBranches('')
                  isToApprove(true)
                }
              }
            }
          }
        }
      }
    }
  }
}