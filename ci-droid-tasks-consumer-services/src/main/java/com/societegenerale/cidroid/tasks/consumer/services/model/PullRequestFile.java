package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PullRequestFile {

    private String sha;

    private String filename;

    private String status;

    private int additions;

    private int deletions;

    private int changes;
}
