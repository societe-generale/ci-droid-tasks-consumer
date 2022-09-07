package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestFile {

    private String sha;

    private String filename;

    private String status;

    private int additions;

    private int deletions;

    private int changes;

    public com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestFile toStandardPullRequestFile(){

        return com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestFile
                .builder()
                .sha(this.sha)
                .filename(this.filename)
                .status(this.status)
                .additions(this.additions)
                .deletions(this.deletions)
                .changes(this.changes)
                .build();
    }

}
