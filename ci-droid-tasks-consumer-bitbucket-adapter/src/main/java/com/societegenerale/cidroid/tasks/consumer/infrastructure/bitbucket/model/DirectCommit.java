package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class DirectCommit {

    private String branch;

    @JsonProperty("message")
    private String commitMessage;

    @JsonProperty("content")
    private String base64EncodedContent;

    @JsonProperty("sha")
    private String previousVersionSha1;

    private Committer committer;

    @Data
    @AllArgsConstructor
    public static class Committer {

        private String name;

        private String email;

    }

    public static DirectCommit from(com.societegenerale.cidroid.tasks.consumer.services.model.DirectCommit stdCommit) {

        var bitbucketCommit=new DirectCommit();

        bitbucketCommit.setBranch(stdCommit.getBranch());
        bitbucketCommit.setCommitMessage(stdCommit.getCommitMessage());
        bitbucketCommit.setCommitter(new Committer(stdCommit.getCommitter().getName(),stdCommit.getCommitter().getEmail()));
        bitbucketCommit.setBase64EncodedContent(stdCommit.getBase64EncodedContent());
        bitbucketCommit.setPreviousVersionSha1(stdCommit.getPreviousVersionSha1());

        return bitbucketCommit;
    }
}
