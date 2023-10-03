# CI-Droid tasks-consumer

[![Build Status](https://travis-ci.org/societe-generale/ci-droid-tasks-consumer.svg?branch=master)](https://travis-ci.org/societe-generale/ci-droid-tasks-consumer)
[![Coverage Status](https://coveralls.io/repos/github/societe-generale/ci-droid-tasks-consumer/badge.svg?branch=master)](https://coveralls.io/github/societe-generale/ci-droid-tasks-consumer?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.societegenerale.ci-droid.tasks-consumer/ci-droid-tasks-consumer-services/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.societegenerale.ci-droid.tasks-consumer/ci-droid-tasks-consumer-services)
[![Project Map](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/societegeneralecidroidtasksconsumer/)

CI-droid tasks consumer will execute the boring tasks on behalf of the dev teams.


Main documentation is available [here](https://github.com/societe-generale/ci-droid).


## Getting started

The recommended way of running CI-droid-tasks-consumer, is to create a new project and import ci-droid-tasks-consumer-starter in its [latest version](https://mvnrepository.com/artifact/com.societegenerale.ci-droid.tasks-consumer/ci-droid-tasks-consumer-starter), depending on your build tool. 

It is a Spring project, so you'll need to place your `application.yml` file in `src/main/resources`, create an application class in `src/main/java`, and  launching the application should be enough to get something : 

```java
@SpringBootApplication
public class CiDroidTasksConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiDroidTasksConsumerApplication.class, args);
    }
}
```


since v2.0, we have revisited the configuration to make it easier to configure various source control systems (not at the same time though) : not all features are enabled for all source control, but you would be able to get it started for :
- GitHub 
- GitLab
- Azure Devops
- Bitbucket (since 2.1)

Each has its own adapter in its own module, that implements some of the features defined in the generic interfaces :
- [SourceControlEventsReactionPerformer](./blob/master/ci-droid-tasks-consumer-services/src/main/java/com/societegenerale/cidroid/tasks/consumer/services/SourceControlEventsReactionPerformer.java)
- [SourceControlBulkActionsPerformer](./blob/master/ci-droid-tasks-consumer-services/src/main/java/com/societegenerale/cidroid/tasks/consumer/services/SourceControlBulkActionsPerformer.java)

Below are elements of the `application.yml` you need to provide.

### Configuring the source control

#### GitHub

```yaml
source-control:
  type: "GITHUB"
  # either the public GitHub URL, or the self-hosted URL if you are in that case 
  url: YOUR_GITHUB_URL
  apiToken: "YOUR_API_TOKEN"
# optional - if you enable the rebasing behavior, you can provide some Git credentials that will be used to perform the rebase on open PRs : 
  login: "SOME_LOGIN"
  password: "SOME_PASSWORD"
```

#### GitLab

```yaml
source-control:
  type: "GITLAB"
  # either the public GitLab URL, or the self-hosted URL if you are in that case 
  url: YOUR_GITLAB_URL
  apiToken: "YOUR_API_TOKEN"
```

#### AzureDevops

There's no URL to configure, since there's only one URL and no "on-prem" offer. 

```yaml
source-control:
    type: "AZURE_DEVOPS"
    apiToken: "YOUR_API_TOKEN"
    # it will be aware of the repositories within a project, which is itself in an organization
    # so we need to configure both, separated by a '#'
    organization-name: "YOUR_ORG#YOUR_PROJECT"
```

#### Bitbucket

```yaml
source-control:
    type: "BITBUCKET"
    # the URL, of the form http://HOSTNAME/api/projects/public-project"
    url: "YOUR_BITBUCKET_URL"
  # optional - if you enable the rebasing behavior, you can provide some Git credentials that will be used to perform the rebase on open PRs : 
    login: "SOME_LOGIN"
    password: "SOME_PASSWORD"
    # The Bitbucket project under which your repositories are  
    # In most cases, it should be the same as the end of the URL configured above 
    project-key: "public-project"
```
    
### Sync mode 

In regular mode, CI-droid-tasks-controller receives events from an event bus (RabbitMq or Kafka, which are supported by Spring Cloud) : since processing a source control event may take some time, we usually prefer to respond immediately to source the control and acknowledge the request, then process it asynchronously. 

However, especially while prototyping, we may want a simpler setup without a messaging bus and CI-droid may be preferable. 

For those situations, since v1.3.0, it's possible to start ci-droid-tasks-consumer in sync mode : instead of listening for events on the message bus, it will now listen to messages on a REST interface on `/cidroid-sync-webhook` endpoint.

```yaml
synchronous-mode: true
spring:
 # disable rabbit (or Kafka) autoconfig
  autoconfigure.exclude: org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```
