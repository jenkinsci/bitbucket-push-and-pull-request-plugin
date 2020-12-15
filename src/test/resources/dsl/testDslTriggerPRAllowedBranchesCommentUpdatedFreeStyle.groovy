/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestUpdatedAction("**")
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
                bitBucketPPRPullRequestCommentUpdatedActionFilter {
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