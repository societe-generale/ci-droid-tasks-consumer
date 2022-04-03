package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(converter = GitLabPushEventSanitizer.class)
public class GitLabPushEvent extends PushEvent implements GitLabEvent {

    private GitLabProject project;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("total_commits_count")
    private int nbCommits;

    private List<Commit> commits;

    private String ref;

    private Repository repository;

    @Override
    public Commit getHeadCommit() {
        throw new UnsupportedOperationException("headCommit is not implemented yet for GitLab");
    }

    @Nonnull
    public List<Commit> getCommits() {

        if(commits==null){
            return Collections.emptyList();
        }

        return commits;
    }

}
