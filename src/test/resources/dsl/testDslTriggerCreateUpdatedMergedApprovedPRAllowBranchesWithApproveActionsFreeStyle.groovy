/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction("**", true)
      pullRequestUpdatedAction("**", true)
      pullRequestMergedAction("**", true)
      pullRequestApprovedAction(false, "**", true)
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
                  allowedBranches("**")
				  isToApprove(true)
                }
              }
            }
            bitBucketPPRPullRequestTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestUpdatedActionFilter {
                  allowedBranches("**")
				  isToApprove(true)
                }
              }
            }
            bitBucketPPRPullRequestTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestMergedActionFilter {
                  allowedBranches("**")
				  isToApprove(true)
                }
              }
            }
            bitBucketPPRPullRequestTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestApprovedActionFilter {
                  triggerOnlyIfAllReviewersApproved(false)
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