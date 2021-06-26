pipelineJob('test-job') {
  properties {
    pipelineTriggers {
      triggers {
        bitBucketTrigger {
          triggers {
            bitBucketPPRPullRequestTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestDeclinedActionFilter {
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