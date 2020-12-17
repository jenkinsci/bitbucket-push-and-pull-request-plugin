/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction()
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
            bitBucketPPRPullRequestTriggerFilter {
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