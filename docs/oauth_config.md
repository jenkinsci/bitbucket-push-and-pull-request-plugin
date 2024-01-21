
## Warning
This info is not created by the maintainer of the package. It is how I got it working by trial, error and looking at the code

## Background
* [Atlassian support page about Oauth Consumers](https://support.atlassian.com/bitbucket-cloud/docs/use-oauth-on-bitbucket-cloud/)

## Steps

### Create bitbucket.org oauth client

* go to https://bitbucket.org/yourorganisation/workspace/settings/api (cogwheel -> workspace settings -> oauth consumer)
* Add private consumer
  
![example consumer](./img/bitbucket_oauth2_client.png)
* Expand the consumer to show key and secret

![consumer key and secret](./img/bitbucket_oauth2_keyandsecret.png)

### Create Jenkins credential

* go to jenkins credential manager https://jenkinsurl/manage/credentials/store/system/domain/_/
* Add credential
* Type : Secret text
* Secret : the Secret from bitbucket oauth consumer
* ID : the Key from bitbucket oauth consumer
* Description : 

![jenkins credential](./img/jenkins_oauth_secret.png)

### Jenkins Job config

Use the credential in the `Build with Butbucket Push and Pull Request Plugin` trigger

![jenkins credential](./img/jenkins_job_trigger.png)

