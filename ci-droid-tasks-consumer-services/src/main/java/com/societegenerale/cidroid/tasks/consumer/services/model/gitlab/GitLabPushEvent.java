package com.societegenerale.cidroid.tasks.consumer.services.model.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(converter = GitLabPushEvent.GitLabPushEventSanitizer.class)
public class GitLabPushEvent extends PushEvent {

    private GitLabProject project;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("user_name")
    private String userName;

    private String ref;

    @Override
    public Commit getHeadCommit() {
        throw new UnsupportedOperationException("headCommit is not implemented yet for GitLab");
    }


    private Repository repository;

    private String rawMessage;

    /**
     * Contrary to Github, some GitLab infos are stored in the 'project' attribute, not the 'repository'.
     * Using this converter to build a "standard" PushEvent
     */
    static class GitLabPushEventSanitizer extends StdConverter<GitLabPushEvent,GitLabPushEvent> {

        @Override
        public GitLabPushEvent convert(GitLabPushEvent gitLabPushEvent) {

            gitLabPushEvent.getRepository().setDefaultBranch(gitLabPushEvent.getProject().getDefaultBranch());

            gitLabPushEvent.getRepository().setId(gitLabPushEvent.getProject().getId());

            return gitLabPushEvent;
        }

    }
}
