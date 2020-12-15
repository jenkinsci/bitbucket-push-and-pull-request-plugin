/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestUpdatedAction("**", true)
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
                bitBucketPPRPullRequestUpdatedActionFilter {
                  allowedBranches("**")
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