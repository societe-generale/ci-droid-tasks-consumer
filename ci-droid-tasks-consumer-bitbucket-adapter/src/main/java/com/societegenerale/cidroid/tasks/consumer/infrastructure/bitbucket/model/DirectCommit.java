package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import feign.form.FormProperty;
import lombok.Data;

import java.util.Base64;

@Data
public class DirectCommit {

    private String branch;

    @FormProperty("message")
    private String commitMessage;

    private String content;

    @FormProperty("sourceCommitId")
    private String previousVersionSha1;

    public static DirectCommit from(com.societegenerale.cidroid.tasks.consumer.services.model.DirectCommit stdCommit) {

        var bitbucketCommit=new DirectCommit();

        bitbucketCommit.setBranch(stdCommit.getBranch());
        bitbucketCommit.setCommitMessage(stdCommit.getCommitMessage());
        bitbucketCommit.setContent(new String(Base64.getDecoder().decode(stdCommit.getBase64EncodedContent())));
        bitbucketCommit.setPreviousVersionSha1(stdCommit.getPreviousVersionSha1());

        return bitbucketCommit;
    }
}
