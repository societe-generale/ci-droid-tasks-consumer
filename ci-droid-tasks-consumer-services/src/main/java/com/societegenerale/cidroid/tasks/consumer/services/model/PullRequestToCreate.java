package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PullRequestToCreate {

    private String title;

    private String body;

    private String head;

    private String base;

}
