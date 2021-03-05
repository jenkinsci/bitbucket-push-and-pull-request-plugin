freeStyleJob('test-job1') {
  triggers {
    bitbucketTriggers {
      pullRequestCreatedAction()
    }
  }
}
freeStyleJob('test-job2') {
  triggers {
    bitBucketTrigger {
      triggers {
        bitBucketPPRPullRequestTriggerFilter {
          actionFilter {
            bitBucketPPRPullRequestUpdatedActionFilter {
              isToApprove(true)
            }
          }
        }
      }
    }
  }
}
pipelineJob('test-job3') {
  properties {
    pipelineTriggers {
      triggers {
        bitBucketTrigger {
          triggers {
            bitBucketPPRRepositoryTriggerFilter {
              actionFilter {
                bitBucketPPRRepositoryPushActionFilter {
                  triggerAlsoIfTagPush(false)
                  triggerAlsoIfNothingChanged(false)
                  allowedBranches('')
                }
              }
            }
            bitBucketPPRPullRequestTriggerFilter {
              actionFilter {
                bitBucketPPRPullRequestCommentDeletedActionFilter {
                }
              }
            }
          }
        }
      }
    }
  }
}