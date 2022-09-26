package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class RepositoryToCreatePullRequest {
    private Integer id;
    private String slug;
    private Project project;
    private LinksWithClone links;
    private boolean forkable;

    public Optional<Repository> toStandardRepo(String branch) {
        return Optional.of(com.societegenerale.cidroid.tasks.consumer.services.model.Repository.builder()
                .fullName(slug)
                .cloneUrl(links.getHttpCloneURL())
                .defaultBranch(branch)
                .fork(forkable)
                .id(id)
                .name(slug)
                .url(links.getSelf().stream().findFirst().get().getHref())
                .build()
        );
    }

}
