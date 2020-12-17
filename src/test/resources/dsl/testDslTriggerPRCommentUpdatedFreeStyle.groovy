/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestCommentUpdatedAction()
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
                }
              }
            }
          }
        }
      }
    }
  }
}