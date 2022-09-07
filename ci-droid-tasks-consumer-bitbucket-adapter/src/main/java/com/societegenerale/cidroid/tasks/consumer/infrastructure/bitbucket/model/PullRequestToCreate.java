package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PullRequestToCreate {

    private String title;

    private String body;

    private String head;

    private String base;

    public static PullRequestToCreate from(com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate newPr) {

        return PullRequestToCreate.builder()
                .title(newPr.getTitle())
                .body(newPr.getBody())
                .head(newPr.getHead())
                .base(newPr.getBase())
                .build();


    }

}
