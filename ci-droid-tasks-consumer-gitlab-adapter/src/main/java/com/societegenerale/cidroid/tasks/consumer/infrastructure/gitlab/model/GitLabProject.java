package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabProject {

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("path_with_namespace")
    private String fullName;

    private int id;

}
