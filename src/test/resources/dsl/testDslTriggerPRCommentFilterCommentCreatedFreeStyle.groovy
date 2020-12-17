/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentCreatedAction("**", "text")
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
                bitBucketPPRPullRequestCommentCreatedActionFilter  {
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