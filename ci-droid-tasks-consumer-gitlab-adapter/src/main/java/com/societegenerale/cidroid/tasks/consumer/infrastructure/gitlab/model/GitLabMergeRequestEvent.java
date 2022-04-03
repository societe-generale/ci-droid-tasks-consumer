package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(converter = GitLabMergeRequestEventSanitizer.class)
public class GitLabMergeRequestEvent extends PullRequestEvent implements GitLabEvent{

    private GitLabProject project;

    private int prNumber;

    public String action;

    private Repository repository;

    @JsonProperty("object_attributes")
    private void unpackNestedAttributes(Map<String,Object> attributes) {
        this.prNumber = (Integer) attributes.get("id");
    }
}
