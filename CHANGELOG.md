# Change Log

## 3.1.4 (2024-10-29)

### Closed Issues
- #358

## What's Changed
* Use `jenkins.baseline` to reduce bom update mistakes by @strangelookingnerd in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/356
* Changed model for Bitbucket Server Payload by @RMadsenG in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/359

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.1.4...bitbucket-push-and-pull-request-3.1.5

## 3.1.3 (2024-10-29)
* Fix: #350 and #348 

### Closed Issues
- #350
- #348

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.1.2...bitbucket-push-and-pull-request-3.1.3

## 3.1.2 (2024-10-14)
* add git plugin interaction explanation and status propagation feature… by @julioc-p in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/342
* 198 skipping resolution of commit since it originates from another repository by @julioc-p in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/341
* Reformat declarative pipeline by @julioc-p in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/344
* use merge commit for build if event is pull request fulfilled by @julioc-p in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/346
* update help so that it does not recommend the refs/heads branch speci… by @julioc-p in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/345

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.1.1...bitbucket-push-and-pull-request-3.1.2

## 3.1.1 (2024-09-19)

### What's Changed
* Fix: Fix npe for propagation url settings by @solarlodge in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/340

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.1.0...bitbucket-push-and-pull-request-3.1.1

## 3.1.0 (2024-09-12)

### What's Changed
* New field for status propagation of pipeline by @julioc-p in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/338
* add implementation of method getPullRequestUrl() and refactoring. by @cdelmonte-zg  #339

### Closed Issues
- #331
- #332 
- #334 
- #337 

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.0.3...bitbucket-push-and-pull-request-3.1.0
## 3.0.3 (2024-08-16)

### What's Changed
* Use "get" prefix for configuration attribute getters by @laudrup in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/328
* #334 - fix log message pattern to include parameter index by @Gozke in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/335
* Enable Jenkins Security Scan by @strangelookingnerd in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/333

### New Contributors
* @laudrup made their first contribution in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/328
* @strangelookingnerd made their first contribution in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/333

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.0.2...bitbucket-push-and-pull-request-3.0.3

## 3.0.3 (2024-08-16)

### What's Changed
* Use "get" prefix for configuration attribute getters by @laudrup in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/328
* #334 - fix log message pattern to include parameter index by @Gozke in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/335
* Enable Jenkins Security Scan by @strangelookingnerd in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/333

### New Contributors
* @laudrup made their first contribution in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/328
* @strangelookingnerd made their first contribution in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/333

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.0.2...bitbucket-push-and-pull-request-3.0.3

## 3.0.2 (2024-01-31)

### What's Changed
Added Documentation on Bitbucket Cloud integration for build status propagation back to BB-cloud (tnx @ccecvb))

### Closed Issues:
#316 Documentation on Bitbucket Cloud integration for build status propagation back to BB-cloud

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.0.1...bitbucket-push-and-pull-request-3.0.2

## 3.0.1 (2023-12-08)

### What's Changed
- Fix issue #314 
- Fix issue #310 

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-3.0.0...bitbucket-push-and-pull-request-3.0.1

## 3.0.0 (2023-10-14)

### What's Changed
* Porting to Java 11
* Add new feature "Add option for single triggered job", thanks to @solarlodge 
* Fix issue #306

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.8.4...bitbucket-push-and-pull-request-3.0.0

## 2.8.4 (2023-08-30)

### What's Changed
* fix for security issue security-3165

## 2.8.3 (2022-07-29)

### What's Changed
* fix log by @cdelmonte-zg in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/274

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.8.2...bitbucket-push-and-pull-request-2.8.3

## 2.8.2 (2022-07-10)

### What's Changed
* Replace git.io with expanded URLs by @MarkEWaite in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/253
* Bitbuckt PPR Plugin: request failed. #243 , jenkins pipeline job not… by @cdelmonte-zg in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/255
*  Reduce log level to Fine #269 

### Fixed Bugs
- #241 Jenkins pipeline job not triggered on created pull request 
- #243 Bitbuckt PPR Plugin: request failed.
- [JENKINS-67917 ](https://issues.jenkins.io/browse/JENKINS-67917)

### New Contributors
* @MarkEWaite made their first contribution in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/253
* @jsj1027  made their  first contribution in https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/269

**Full Changelog**: https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.8.1...bitbucket-push-and-pull-request-2.8.2

## News
- Bug fix for state notification

## Relevant Pull requests closed 
#238 fix

## 2.8.0 (2021-11-27)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.7.4...bitbucket-push-and-pull-request-2.8.0)

## News
- Added dedicated credentials for at global and at job level to use the Bitbucket Rest API
- Added Action for the event "Pull Request Declined"
- Distinction between "unapprove if job fails" and "decline if job fails"

## Relevant Pull requests closed 
- #225 Feature/update readme
- #221 decline on fail 
- #219 Gozke feature/173 trigger on pr decline
- #206 Feature/consumer auth

## Relevant tickets closed
- #220 add a trigger action for denied PR
- #214 Support ssh-keys to propagate build status to Bitbucket
- #199 Don't decline PRs whenever jenkins job is failed
- #195 Failed pipeline should not decline the PR and just report on the PR

## 2.7.4 (2021-11-17)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.7.3...bitbucket-push-and-pull-request-2.7.4)

## News
- Fixed following bug: _It happens from time to time that Bitbucket fires different events shortly one after the other for the same repo. In such cases builds have to queue up and the end result is usually that the build status notification gets mixed up for some of the events._

## Pull requests closed 
- #184 Resolving build key mix-up, by Gozke (tnx)

## Relevant tickets closed
- #195 [help] Failed pipeline should not decline the PR and just report on the PR
- #194 One more pipeline starts during PR update/merge
- #186 HTTP ERROR 403 No valid crumb was included in the request investigating
- #183 [Bug] Queued up builds send notifications to Bitbucket with mixed up key stale

## 2.7.3 (2021-11-10)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.7.2...bitbucket-push-and-pull-request-2.7.3)

## News
- Refactoring
- Making it possible to use job name a build key 

## Pull requests closed 
- #197 Refactoring
- #181 Making it possible to use job name a build key 

## Relevant tickets closed
- #180 Make it possible to use job's name as build key during back-propagation enhancement
- #169  Bitbucket Trigger for job is not present
- #172 bitbucket-push-and-pull-request-plugin webhook is not triggering based push


## 2.7.2 (2021-03-30)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.7.1...bitbucket-push-and-pull-request-2.7.2)

**Implemented enhancements:**

- Added env vars for latest commit hashes from source and destination branches

**Pull requests closed:**

- #171 Add env vars for latest commit hashes from source and destination branches docs views

**Relevant tickets closed:**

- [Investigation] #163 Only builds latest commit on master if no Git filter matches source branch


## 2.7.1 (2021-03-07)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.7.0...bitbucket-push-and-pull-request-2.7.1)

**Fixed bugs:**

- Removed deprecation warning on `triggers()` closure for freestyle jobs (it only affects Pipelines).
- Updated unit tests and docs accordingly.

**Pull requests closed:**

- #168 hotfix/proper triggers dsl deprecation (tnx @rhotau) 

**Relevant tickets closed:**

- [Fix] #166 Job DSL configuration failing
- [Fix] #143 [Docs] Missing DSL

## 2.7.0 (2021-02-18)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.6.4...bitbucket-push-and-pull-request-2.7.0)

**Implemented enhancements:**

- Added __global configuration__ 
- Added "silence notifications" option in global configuration
- Added "override default webhook endpoint" option in global configuration

**Relevant tickets closed**

- [Enhancement] #154 Add global configuration 
- [Enhancement] #142 Silence notifications enhancement (tnx @SirMrDexter)

## 2.6.4 (2020-12-18)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.6.3...bitbucket-push-and-pull-request-2.6.4)

**Implemented enhancements:**

- Handle for deprecation of DSL triggers{}
 Since job-dsl plugin v1.77, triggers{} has been marked deprecated (see details), this pull request is a proposal of how to handle it by relying solely on properties{}.
(tnx @rhotau )

**Fixed bugs:**

- Fixed problem with PR that trigger simultaneously will override one another 
(tnx @hexonxons )   

**Relevant tickets closed**

* [Bug] #146 PR that trigger simultaneously will override one another  


## 2.6.3 (2020-11-27)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.6.2...bitbucket-push-and-pull-request-2.6.3)

**Fixed bugs:**

* Fixed bug BITBUCKET_X_EVENT not populated with full eventKey. 

**Relevant tickets closed**

* [Bug] #152 BITBUCKET_X_EVENT not populated with full eventKey. 

## 2.6.2 (2020-11-25)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.6.1...bitbucket-push-and-pull-request-2.6.2)

**Implemented enhancements:**

* Improving logging  

## 2.6.1 (2020-11-23)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.6.0...bitbucket-push-and-pull-request-2.6.1)

**Implemented enhancements:**

* Added multibranch pipeline jobs support for BB Server  

**Relevant tickets closed:**

* [Bug]  #149 refs/heads/<branchName> spec in Bitbucket Server push action seems not working properly

## 2.6.0 (2020-11-13)
[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.5.2...bitbucket-push-and-pull-request-2.6.0)

**Implemented enhancements:**

* Added back propagation for build status and approve form BB Server  
* Documentation improved

**Relevant tickets closed:**

* [Bug]  #113 Build status "in progress" will be sent when the build is completed  


## 2.5.2 (2020-10-30)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.5.1...bitbucket-push-and-pull-request-2.5.2)

**Implemented enhancements:**

* Added environment variables for multiconfiguration jobs
* Added description of Bitbucket Push and Pull Request environment variables in the Jenkins list of available environment variables.

**Relevant tickets closed:**

* [Enhancement] missing environment variables for multi configuration #132

## 2.5.1 (2020-10-28)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.5.0...bitbucket-push-and-pull-request-2.5.1)

**Implemented enhancements:**

* Bug fix for multi branch push
* New environment variable for bitbucket-x-event header

**Relevant tickets closed:**

* [Bug] Cloud Push doesn't trigger multi branch pipeline job #134 , #78 
* [Enhancement] `BITBUCKET_X_EVENT` added to environment variables #130  


## 2.5.0 (2020-10-14)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.4.0...bitbucket-push-and-pull-request-2.5)

**Implemented enhancements:**

* Add possibility to trigger jobs only on tag pushes (for Bitbucket Cloud and Bitbucket Server)
* Add trigger for PR comment created (for Bitbucket Server)

**Relevant tickets closed:**

* [Feature] Triggering a job only when tags are pushed #119 
* [Feature] Trigger builds on new tag only #106 

## 2.4.0 (2020-06-20)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.3.3...bitbucket-push-and-pull-request-2.4.0)

**Implemented enhancements:**

* Added build state propagation (for Bitbucket Cloud)
* Added Approve push and pull request by Jenkins on successful build (for Bitbucket Cloud)

**Relevant tickets closed:**

* [Feature] Propagate build status to Bitbucket #23 
* [Feature] Approve pull request on successful build #29

## 2.3.3 (2020-04-19)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.3.2...bitbucket-push-and-pull-request-2.3.3)

**Implemented enhancements:**

* Added triggers for Comments on pull requests from BB Cloud 

**Relevant tickets closed:**

* Create a BucketPPRPullRequestCommentCreatedFilter #64 

## 2.3.2 (2020-04-07)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.3.1...bitbucket-push-and-pull-request-2.3.2)

**Implemented enhancements:**

* Verified compatibility with BitBucket Server 7.0
* Added trigger for "Source branch updated" event of BitBucket Server >= 7.0

**Relevant tickets closed:**

* New "Source branch updated" PR Webhook/Event supported in BBS 7.0 #97 

## 2.3.1 (2020-03-30)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.3.0...bitbucket-push-and-pull-request-2.3.1)

**Implemented enhancements:**

* Feature: New Environment Variables
* More tests
* Code style fixes
* Refactoring
* Updated documentation

## Closed tickets

* Doesn't work branch filter for Cloud BitBucket pull requests #86 
* Webhook does not trigger build on pull request creation #98 
* Not Working With the Pipeline, Kindly help me on this! #63

## 2.3.0 (2020-02-10)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.2.0...bitbucket-push-and-pull-request-2.3.0)

**Implemented enhancements:**
- Moved wiki files to Github
- Improved documentation
- Refactoring
- Code Style fixes
- Add filter for branches on pull requests

**Bug fixes**
- Issue #71: Polling is not available

## 2.2.0 (2019-10-14)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-2.0.0...bitbucket-push-and-pull-request-2.2.0)

**Implemented enhancements:**

- Added checkbox to define if changes on the repos have to be confirmed through the git plugin before starting a job triggered by a push
- Added pull request support for Mercurial on Bitbucket Cloud
- Added support for multiple triggers

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
- #48 Add pull request support for Bitbucket Server and Mercurial on Bitbucket Cloud

## 1.6.4 (2019-06-19)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.3...bitbucket-push-and-pull-request-1.6.4)

**Implemented enhancements:**

- Added push support for Mercurial on Bitbucket Cloud

**Closed issues:**

- Issue #38 BITBUCKET\*SOURCE_BRANCH has wrong value for repo:push events
  - Issue #36 Branch expression matching before triggering the build

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

- #41 BranchSpec pattern matching directions (by macghriogair)
- #34 Create CODE_OF_CONDUCT.md (by eiriarte-mendez)

## 1.6.2 (2019-05-10)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.1...bitbucket-push-and-pull-request-1.6.2)

**Closed issues:**

- Issue #27 Builds not triggering with 1.6.1 and Bitbucket Server 7.0.1 bug

**Merged pull requests:**

- Builds not triggering with 1.6.1 and Bitbucket Server 7.0.1 #28

## 1.6.1 (2019-05-08)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.6.0...bitbucket-push-and-pull-request-1.6.1)

**Implemented enhancements:**

- added more enviroment variables and improved the documentation about them

**Closed issues:**

- Improvements#24 Add environment variable for pull request id
- Issue#19 Webhook not triggering
- Improvements#11 BITBUCKET_SOURCE_BRANCH is not injected for SCM poll

**Merged pull requests:**

- _develop_ Enhance environment variables #25

## 1.6.0 (2019-04-29)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/bitbucket-push-and-pull-request-1.5.0...bitbucket-push-and-pull-request-1.6.0)

**Implemented enhancements:**

- split methods of io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe to allow unit tests

**Closed issues:**

- Improvements#20 Add trigger for pull request merged
- Improvements#14 Pattern for allowed branches
- Improvements#5 Improve support for BitBucket Server push

**Merged pull requests:**

- Remove deprecated username field from payload #17 by macghriogair
- add support for pattern matching on branches #18 by macghriogair
- Add support for merged pull requests #21 by cdelmonte-zg

## 1.5.0 (2019-04-11)

[Full Changelog](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/compare/master@%7B20days%7D...master)

**Implemented enhancements:**

- split methods of io.jenkins.plugins.bitbucketpushandpullrequest.BitBucketPPRJobProbe to allow unit tests

**Closed issues:**

- Improvements#10 add ssh uri matching for git clone
- Improvements#12 No change-logs or description of features
- Improvements#5 Improve support for BitBucket Server push

**Merged pull requests:**

- gitignore more project specific entries [#13](https://github.com/jenkinsci/bitbucket-push-and-pull-request-plugin/pull/13) by macghriogair
