/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction()
      pullRequestUpdatedAction()
      pullRequestApprovedAction(false)
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
            bitBucketPPRPullRequestTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestUpdatedActionFilter {
                }
              }
            }
            bitBucketPPRPullRequestTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestApprovedActionFilter {
                  triggerOnlyIfAllReviewersApproved(false)
                }
              }
            }
          }
        }
      }
    }
  }
}