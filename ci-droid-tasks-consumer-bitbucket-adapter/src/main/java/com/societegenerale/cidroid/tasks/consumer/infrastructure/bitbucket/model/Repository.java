package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    private String defaultBranch;

    @JsonProperty("slug")
    private String fullName;

    private String name;

    private boolean forkable;

    private SelfWithClone links;

    private int id;

    public Optional<com.societegenerale.cidroid.tasks.consumer.services.model.Repository> toStandardRepo() {

        return Optional.of(
                com.societegenerale.cidroid.tasks.consumer.services.model.Repository.builder()
                        .fullName(this.fullName)
                        .cloneUrl(this.links.getHttpCloneURL())
                        .defaultBranch(this.defaultBranch)
                        .fork(this.forkable)
                        .id(this.id)
                        .name(this.name)
                        .url(this.links.getSelf().stream().findFirst().get().getHref())
                        .build()
        );



    }
}
