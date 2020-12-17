/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentUpdatedAction("**", "text")
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
                bitBucketPPRPullRequestCommentUpdatedActionFilter  {
                  allowedBranches("**")
				  commentFilter("text")
                }
              }
            }
          }
        }
      }
    }
  }
}