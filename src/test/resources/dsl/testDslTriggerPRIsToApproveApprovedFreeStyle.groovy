/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestApprovedAction(false, '', true)
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
                bitBucketPPRPullRequestApprovedActionFilter {
                  triggerOnlyIfAllReviewersApproved(false)
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