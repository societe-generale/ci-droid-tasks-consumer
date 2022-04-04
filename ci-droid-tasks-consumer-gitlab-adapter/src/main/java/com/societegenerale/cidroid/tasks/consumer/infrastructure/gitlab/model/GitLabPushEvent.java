package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import lombok.Data;

import static java.util.stream.Collectors.toList;

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

    @JsonIgnore
    private List<GitLabCommit> gitLabCommits;

    public void setCommits(List<GitLabCommit> gitLabCommits){
        this.gitLabCommits=gitLabCommits;
    }

    private String ref;

    @JsonIgnore
    private Repository repository;

    @Override
    public void setRepository(Repository repo) {
        this.repository=repo;
    }

    private String rawEvent;

    @Override
    public Commit getHeadCommit() {
        throw new UnsupportedOperationException("headCommit is not implemented yet for GitLab");
    }

    @Nonnull
    public List<Commit> getCommits() {

        if(gitLabCommits==null){
            return Collections.emptyList();
        }

        return gitLabCommits.stream().map(GitLabCommit::toStandardCommit).collect(toList());
    }

    @Override
    public void setRawEvent(String rawEvent) {
        this.rawEvent=rawEvent;
    }


}
