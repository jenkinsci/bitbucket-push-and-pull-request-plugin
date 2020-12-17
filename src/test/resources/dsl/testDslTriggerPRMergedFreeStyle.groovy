/*
freeStyleJob('test-job') {
  triggers {
    bitbucketTriggers {
      pullRequestMergedAction()
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
                }
              }
            }
          }
        }
      }
    }
  }
}