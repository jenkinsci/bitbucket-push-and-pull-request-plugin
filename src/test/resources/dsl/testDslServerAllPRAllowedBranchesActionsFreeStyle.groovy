/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestServerCreatedAction("*")
      pullRequestServerUpdatedAction("*")
      pullRequestServerSourceUpdatedAction("*")
      pullRequestServerApprovedAction(false, "*")
      pullRequestServerMergedAction("*")
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
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerCreatedActionFilter  {
                  allowedBranches("*")
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerUpdatedActionFilter {
                  allowedBranches("*")
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerSourceUpdatedActionFilter {
                  allowedBranches("*")
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerApprovedActionFilter {
                  triggerOnlyIfAllReviewersApproved(false)
                  allowedBranches("*")
                }
              }
            }
            bitBucketPPRPullRequestServerTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestServerMergedActionFilter {
                  allowedBranches("*")
                }
              }
            }
          }
        }
      }
    }
  }
}