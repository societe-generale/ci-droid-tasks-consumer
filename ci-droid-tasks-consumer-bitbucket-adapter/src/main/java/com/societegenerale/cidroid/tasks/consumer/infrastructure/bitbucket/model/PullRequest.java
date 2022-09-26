package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
//Can be deleted as this might be same as PullRequestToCreate
public class PullRequest {

    private final int id;

    private Properties properties;

    private Author author;

    private ZonedDateTime createdDate;

    private Links links;

    private FromOrToRef fromRef;

    private FromOrToRef toRef;

    @ConstructorProperties({ "id" })
    @JsonCreator
    public PullRequest(int id) {
        this.id = id;
    }


    public com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest toStandardPullRequest(){

        return com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest.builder()
                .number(this.id)
                .baseBranchName(this.toRef.getDisplayId())
                .branchName(this.fromRef.getDisplayId())
                // need to check the impact of converting Zoned to Local
                .creationDate(this.createdDate.toLocalDateTime())
                .htmlUrl(this.links.getSelf().stream().findFirst().orElse(new Self()).getHref())
                // Duplicate code
                .url(this.links.getSelf().stream().findFirst().orElse(new Self()).getHref())
                .branchStartedFromCommit(this.toRef.getLatestCommit())
                .repo(this.toRef.getRepository().toStandardRepo(this.toRef.getId()).get())
                .user(this.author.getUser().toStandardUser())
                .build();


    }
}
