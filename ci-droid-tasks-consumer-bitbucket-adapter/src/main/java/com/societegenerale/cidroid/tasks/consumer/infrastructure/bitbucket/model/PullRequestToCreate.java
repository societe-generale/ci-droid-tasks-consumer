package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PullRequestToCreate {

    private String title;

    private String description;

    private String state;

    private boolean open;

    private boolean closed;

    private boolean locked;

    private FromOrToRef fromRef;

    private FromOrToRef toRef;

    public static PullRequestToCreate from(com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate newPr,
                                           String repoFullName, String projectKey) {

        RepositoryToCreatePullRequest fromAndToRepo = RepositoryToCreatePullRequest.builder().project(new Project(projectKey))
                .slug(repoFullName).build();

        return PullRequestToCreate.builder()
                .title(newPr.getTitle())
                .description(newPr.getTitle())
                .state("OPEN")
                .open(true)
                .closed(false)
                // reviewers can be configured
                .fromRef(FromOrToRef.builder().id(newPr.getHead()).repository(fromAndToRepo).build())
                .toRef(FromOrToRef.builder().id(newPr.getBase()).repository(fromAndToRepo).build())
                .build();


    }

}
