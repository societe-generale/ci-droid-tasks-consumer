package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    private String url;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("full_name")
    private String fullName;

    private String name;

    private boolean fork;

    @JsonProperty("clone_url")
    private String cloneUrl;

    private int id;

    public Optional<com.societegenerale.cidroid.tasks.consumer.services.model.Repository> toStandardRepo() {

        return Optional.of(
                com.societegenerale.cidroid.tasks.consumer.services.model.Repository.builder()
                        .fullName(this.fullName)
                        .cloneUrl(this.cloneUrl)
                        .defaultBranch(this.defaultBranch)
                        .fork(this.fork)
                        .id(this.id)
                        .name(this.name)
                        .url(this.url)
                        .build()
        );



    }
}
