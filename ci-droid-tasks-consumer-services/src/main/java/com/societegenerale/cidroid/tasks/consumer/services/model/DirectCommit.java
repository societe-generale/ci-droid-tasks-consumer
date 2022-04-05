package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class DirectCommit {

    private String branch;

    private String commitMessage;

    private String base64EncodedContent;

    private String previousVersionSha1;

    private Committer committer;


    @Data
    @AllArgsConstructor
    public static class Committer {

        private String name;

        private String email;

    }
}
