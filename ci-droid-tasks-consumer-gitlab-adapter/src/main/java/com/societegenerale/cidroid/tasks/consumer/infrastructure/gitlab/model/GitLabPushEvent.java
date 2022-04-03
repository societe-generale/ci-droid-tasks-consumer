package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(converter = GitLabPushEventSanitizer.class)
public class GitLabPushEvent implements PushEvent,GitLabEvent {

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

    private String rawEvent;

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

    @Override
    public void setRawEvent(String rawEvent) {
        this.rawEvent=rawEvent;
    }

}
