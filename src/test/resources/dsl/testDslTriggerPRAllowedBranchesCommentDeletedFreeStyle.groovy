/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentDeletedAction("**")
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
                bitBucketPPRPullRequestCommentDeletedActionFilter {
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