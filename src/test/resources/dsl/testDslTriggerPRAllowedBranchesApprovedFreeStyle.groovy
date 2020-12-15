/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestApprovedAction(false, "**")
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
                  allowedBranches("**")
                }
              }
            }
          }
        }
      }
    }
  }
}