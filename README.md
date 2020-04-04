# CI-Droid tasks-consumer

[![Build Status](https://travis-ci.org/societe-generale/ci-droid-tasks-consumer.svg?branch=master)](https://travis-ci.org/societe-generale/ci-droid-tasks-consumer)
[![Coverage Status](https://coveralls.io/repos/github/societe-generale/ci-droid-tasks-consumer/badge.svg?branch=master)](https://coveralls.io/github/societe-generale/ci-droid-tasks-consumer?branch=master)

CI-droid tasks consumer will execute the boring tasks on behalf of the dev teams.


Main documentation is available [here](https://github.com/societe-generale/ci-droid).

## Sync mode 

In regular mode, CI-droid-tasks-controller receives events from an event bus (RabbitMq or Kafka, which are supported by Spring Cloud) : since processing a source control event may take some time, we usually prefer to respond immediately to source the control and acknowledge the request, then process it asynchronously. 

However, especially while prototyping, we may want a simpler setup without a messaging bus and CI-droid may be preferable. 

For those situations, it's possible to start ci-droid-tasks-consumer in sync mode : instead of listening for events on the message bus, it will now listen to messages on a REST interface on `/cidroid-sync-webhook` endpoint.

To achieve this : 

- set `synchronous-mode` property to true
- disable rabbit (or Kafka) autoconfig via `spring.autoconfigure.exclude: org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration` property

