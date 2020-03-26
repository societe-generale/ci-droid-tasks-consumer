package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPushEvent extends PushEvent {

    private String ref;

    private Repository repository;

    @JsonProperty("head_commit")
    private Commit headCommit;

    @Override
    public Repository getRepository() {
        return repository;
    }

    public boolean happenedOnDefaultBranch(){
        return ref.endsWith(getRepository().getDefaultBranch());
    }
}
