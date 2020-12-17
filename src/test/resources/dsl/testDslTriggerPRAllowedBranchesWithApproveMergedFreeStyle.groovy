/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestMergedAction("**", true)
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
                bitBucketPPRPullRequestMergedActionFilter {
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