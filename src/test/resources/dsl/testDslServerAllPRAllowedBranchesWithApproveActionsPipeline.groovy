pipelineJob('test-job') {
  properties {
    pipelineTriggers {
      triggers {
        bitBucketTrigger {
          triggers {
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerCreatedActionFilter  {
                  allowedBranches("*")
				  isToApprove(true)
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerUpdatedActionFilter {
                  allowedBranches("*")
				  isToApprove(true)
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerSourceUpdatedActionFilter {
                  allowedBranches("*")
				  isToApprove(true)
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerApprovedActionFilter {
                  triggerOnlyIfAllReviewersApproved(false)
                  allowedBranches("*")
				  isToApprove(true)
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerMergedActionFilter {
                  allowedBranches("*")
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