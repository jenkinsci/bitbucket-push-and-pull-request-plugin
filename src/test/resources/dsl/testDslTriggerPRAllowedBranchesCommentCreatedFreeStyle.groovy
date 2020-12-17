/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentCreatedAction("**")
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
                bitBucketPPRPullRequestCommentCreatedActionFilter {
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