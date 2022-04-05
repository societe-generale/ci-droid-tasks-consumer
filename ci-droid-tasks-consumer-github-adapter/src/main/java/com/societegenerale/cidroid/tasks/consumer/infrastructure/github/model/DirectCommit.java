package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

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

        var gitHubCommit=new DirectCommit();

        gitHubCommit.setBranch(stdCommit.getBranch());
        gitHubCommit.setCommitMessage(stdCommit.getCommitMessage());
        gitHubCommit.setCommitter(new Committer(stdCommit.getCommitter().getName(),stdCommit.getCommitter().getEmail()));
        gitHubCommit.setBase64EncodedContent(stdCommit.getBase64EncodedContent());
        gitHubCommit.setPreviousVersionSha1(stdCommit.getPreviousVersionSha1());

        return gitHubCommit;
    }
}
