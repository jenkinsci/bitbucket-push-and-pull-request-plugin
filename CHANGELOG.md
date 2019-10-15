# Change Log

## 2.2.0 (2019-10-14)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.0.0...bitbucket-push-and-pull-request-2.2.0)

**Implemented enhancements:**
- Added checkbox to define if changes on the repos have to be confirmed through the git plugin before starting a job triggered by a push 
- Added pull request support for Mercurial on Bitbucket Cloud
- Addes upport for multiple triggers

**Merged pull requests:**
- #60 change pull request 59 and reformatting by cdelmonte-zg
- #59 adding logic to trigger target branch on merge by raghav-a
- #58 an option for repository hasChange() conditional behaviour by cdelmonte-zg
- #55 workflows: implement isPipelineMultibranch check by macghriogair 
- #51 Job DSL multiple triggers by rhotau
- #50 Support for multiple triggers from dsl by rhotau

## 2.0.0 (2019-06-25)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.4...bitbucket-push-and-pull-request-2.0.0)

**Implemented enhancements:**
- Added pull request support for Bitbucket Server
- Added pull request support for Mercurial on Bitbucket Cloud

**Closed issues:**
- Issue #44 Branch expression matching before triggering the build: expecting fix for Bitbucket Server bug
- Issue #37 Jenkins build triggered from push event with Mercurial instead of git enhancement

**Merged pull requests:**
- #49 Add warnings
- #48 [Enhancement] Add pull request support for Bitbucket Server and Mercurial on Bitbucket Cloud

## 1.6.4 (2019-06-19)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.3...bitbucket-push-and-pull-request-1.6.4)

**Implemented enhancements:**
- Added push support for Mercurial on Bitbucket Cloud

**Closed issues:**
- Issue #38 BITBUCKET_SOURCE_BRANCH has wrong value for repo:push events
_ Issue #36 Branch expression matching before triggering the build

**Merged pull requests:**
- #47 Develop
- #46 Improving tests for allowed branches
- #45 Develop

## 1.6.3 (2019-06-14)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.2...bitbucket-push-and-pull-request-1.6.3)

**Closed issues:**
- Issue #36 Branch expression matching before triggering the build
- Issue #30 Version 1.6.2 throws exception after receiving PR payload bug
- Issue #27 Builds not triggering with 1.6.1 and Bitbucket Server 7.0.1 bug
- Issue #26 Add environment variable for git repository url

**Merged pull requests:**
- [fix] #41 BranchSpec pattern matching directions (by macghriogair)
- [enhancement] #34 Create CODE_OF_CONDUCT.md (by eiriarte-mendez)


## 1.6.2 (2019-05-10)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.1...bitbucket-push-and-pull-request-1.6.2)

**Closed issues:**

- Issue #27 Builds not triggering with 1.6.1 and Bitbucket Server 7.0.1 bug

**Merged pull requests:**

- [Bug] #27 Builds not triggering with 1.6.1 and Bitbucket Server 7.0.1 #28


## 1.6.1 (2019-05-08)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.0...bitbucket-push-and-pull-request-1.6.1)

**Implemented enhancements:**

- added more enviroment variables and improved the documentation about them

**Closed issues:**

- Improvements#24 Add environment variable for pull request id
- Issue#19 Webhook not triggering
- Improvements#11 BITBUCKET_SOURCE_BRANCH is not injected for SCM poll

**Merged pull requests:**

- [develop] Enhance environment variables #25 

## 1.6.0 (2019-04-29)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.5.0...bitbucket-push-and-pull-request-1.6.0)

**Implemented enhancements:**

- split methods of io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe to allow unit tests

**Closed issues:**

- Improvements#20 Add trigger for pull request merged
- Improvements#14 Pattern for allowed branches
- Improvements#5 Improve support for BitBucket Server push

**Merged pull requests:**

- [develop] Remove deprecated username field from payload #17 by macghriogair
- [develop] add support for pattern matching on branches #18 by macghriogair
- [develop] Add support for merged pull requests #21 by cdelmonte-zg



## 1.5.0 (2019-04-11)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/master@%7B20days%7D...master)

**Implemented enhancements:**

- split methods of io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe to allow unit tests

**Closed issues:**

- Improvements#10 add ssh uri matching for git clone
- Improvements#12 No change-logs or description of features
- Improvements#5 Improve support for BitBucket Server push

**Merged pull requests:**

- [develop] gitignore more project specific entries [#13](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/13) by macghriogair

