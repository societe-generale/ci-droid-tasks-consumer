# Changelog - see https://keepachangelog.com for conventions

## [Unreleased]

### Added

### Changed
- PR #65 - all tests now running with Junit 5. Junit 4.x totally excluded now.

### Deprecated

### Removed

### Fixed

## [1.1.1] - 2019-03-19

### Added
- PR # 63 - added Spring web and actuator to provide an HTTP healthcheck
- warn log statements when exception during BulkActionToPerform processing
- PR # 64 -  proper handling (through log and notification) of unexpected exception while PR creation 

## [1.1.0] - 2019-03-13

### Added
- Monitoring closing of old PRs now
- PR #58 - handling deleteResource bulk action now

### Changed
- [BREAKING] upgrade to Spring Boot 2.1.3 / Spring Cloud Greenwich SR1. Some properties need to be renamed
- upgraded ci-droid-extensions to 1.0.9

### Fixed
- PR #59 - Code coverage reporting : now taking into account integration tests
- PR #60 - some Codacy violations


## [1.0.12] - 2019-01-10

### Added
- PR #45 - Handler to close old PRs automatically - Thanks [@juliette-derancourt](https://github.com/juliette-derancourt) !!
- PR #54 - Adding some monitoring events around bulk actions

### Changed
- PR #46 - upgraded to ci-droid-api 1.0.6 (CVE fix)

### Removed
- PR #53 - removed test dependency on code-story - Thanks again [@juliette-derancourt](https://github.com/juliette-derancourt) !!

### Fixed
- PR #42 - Send notifications when unexpected exception happens during bulkAction processing
- PR #43 - Sending specific notification when repo mentioned in bulkAction doesn't exist
- PR #51 - using proper parameter name when calling Github API and fetch open PRs 

## [1.0.11] - 2018-12-18

### Fixed
- PR #37 - avoiding to overwrite commits during rebase, in some scenarios

## [1.0.10] - 2018-11-12

### Changed
- issue #28 - bulk actions - don't create a PR if there's already an open one on same branch

### Fixed
- issue #29 - re-adding some dummy classes to instantiate, in case none is instantiated through config. Added auto-config order this time, so that they get instantiated ONLY if required
- issue #31 - MDC related issue when logging specific event

## [1.0.9] - 2018-11-12

### Fixed
- issue #26 - now configuring Github Oauth token for all actions in FeignClient
- issue #27 - now catching runtime exceptions to avoid that a handler throwing it would prevent others from being called

### Removed
- some dummy classes

## [1.0.8] - 2018-11-08

### Changed
- upgraded to ci-droid-extensions 1.0.6
- upgraded to ci-droid-internal-api 1.0.5

### Fixed
- fixed vulnerability CVE-2018-7489 
- issue #22 - avoid NPEs when providing content for certain actionToReplicate

## [1.0.7] - 2018-09-29

### Fixed
- issue #17 - now creating the PR branch on top of the expected branch

## [1.0.6] - 2018-09-20

### Changed
- upgraded to ci-droid-extensions 1.0.5
- upgraded to ci-droid-internal-api 1.0.4

### Fixed
- issue #15 - should use provided pullRequestTitle when creating the PR

## [1.0.5] - 2018-08-25

### Fixed
- issue #13 - in previous version, we had forgotten to send OAuth token when creating PR

## [1.0.4] - 2018-08-17

### Added
- upgraded to ci-droid-extensions 1.0.4 : new actions available

### Changed
- **BREAKING CHANGE IN CONFIG** : renamed property key from gitHub.url to gitHub.api.url 
- issue #8 - now also working with github.com - need to receive an OAuth token instead of password
- not logging full stacktrace anymore when branch already exists


## [1.0.3] - 2018-08-03

### Changed
- issue #4 - if PR is made from a fork don't try to rebase
- upgraded to internal-api and extensions 1.0.2 

### Fixed
- issue #2 - providing a PullRequestEventHandler shouldn't be mandatory
- issue #5 - when credentials are incorrect, send a KO email

## [1.0.2] - 2018-07-12

### Changed

- upgraded to internal-api and extensions 1.0.1
- releasing with Travis


## [1.0.1] - 2018-06-29 

### Changed

- refactoring to follow recommended conventions

## [1.0.0] - 2018-06-21 

first version !


