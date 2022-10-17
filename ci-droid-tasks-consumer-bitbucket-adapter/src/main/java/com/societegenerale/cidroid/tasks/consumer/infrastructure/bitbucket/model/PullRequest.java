package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {

    private final int id;

    private Properties properties;

    private Author author;

    private ZonedDateTime createdDate;

    private Links links;

    private FromOrToRef fromRef;

    private FromOrToRef toRef;

    @ConstructorProperties({"id"})
    @JsonCreator
    public PullRequest(int id) {
        this.id = id;
    }


    public com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest toStandardPullRequest() {

        String href = this.links.getSelf().stream().findFirst().orElse(new Self()).getHref();
        return com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest.builder()
                .number(this.id)
                .baseBranchName(this.toRef.getDisplayId())
                .branchName(this.fromRef.getDisplayId())
                .creationDate(this.createdDate.toLocalDateTime())
                .htmlUrl(href)
                .url(href)
                .branchStartedFromCommit(this.toRef.getLatestCommit())
                .repo(this.toRef.getRepository().toStandardRepo(this.toRef.getId()).get())
                .user(this.author.getUser().toStandardUser())
                .build();


    }
}
