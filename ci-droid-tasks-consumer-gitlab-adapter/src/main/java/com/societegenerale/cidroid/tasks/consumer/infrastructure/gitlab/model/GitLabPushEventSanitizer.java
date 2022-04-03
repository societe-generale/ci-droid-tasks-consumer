package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * Contrary to Github, some GitLab infos are stored in the 'project' attribute, not the 'repository'.
 * Using this converter to build a "standard" PushEvent
 */
public class GitLabPushEventSanitizer extends StdConverter<GitLabPushEvent, GitLabPushEvent> {

        @Override
        public GitLabPushEvent convert(GitLabPushEvent gitLabPushEvent) {
            return (GitLabPushEvent)GitLabEventSanitizerUtils.sanitizeProject(gitLabPushEvent);
        }

}

