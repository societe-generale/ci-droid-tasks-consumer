package com.societegenerale.cidroid.tasks.consumer.services.model.gitlab;

import com.fasterxml.jackson.databind.util.StdConverter;

public class GitLabMergeRequestEventSanitizer extends StdConverter<GitLabMergeRequestEvent, GitLabMergeRequestEvent> {

        @Override
        public GitLabMergeRequestEvent convert(GitLabMergeRequestEvent gitLabPushEvent) {

            gitLabPushEvent.getRepository().setDefaultBranch(gitLabPushEvent.getProject().getDefaultBranch());

            gitLabPushEvent.getRepository().setId(gitLabPushEvent.getProject().getId());


            gitLabPushEvent.getRepository().setFullName(gitLabPushEvent.getProject().getFullName());

            return gitLabPushEvent;
        }

}

