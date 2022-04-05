package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.Map;

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

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {

    private final int number;

    private Boolean mergeable;

    private User user;

    private String url;

    private Repository repo;

    private String branchName;

    private String baseBranchName;

    private String branchStartedFromCommit;

    @JsonProperty("created_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creationDate;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonIgnore
    private boolean isMadeFromForkedRepo;

    @JsonIgnore
    private String warningMessageDuringRebasing;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @ConstructorProperties({ "number" })
    @JsonCreator
    public PullRequest(int number) {
        this.number = number;
    }


    public com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest toStandardPullRequest(){

        return com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest.builder()
                .number(this.number)
                .baseBranchName(this.baseBranchName)
                .branchName(this.branchName)
                .creationDate(this.creationDate)
                .htmlUrl(this.htmlUrl)
                .mergeable(this.mergeable)
                .url(this.url)
                .branchStartedFromCommit(this.branchStartedFromCommit)
                .repo(this.repo.toStandardRepo().get())
                .user(this.user.toStandardUser())
                .build();


    }



    @JsonProperty("base")
    private void unpackNestedBaseProperty(Map<String,Object> base) {
        this.branchStartedFromCommit=(String)base.get("sha");

        this.baseBranchName=(String)base.get("ref");

        this.repo=objectMapper.convertValue(base.get("repo"), Repository.class);
    }

    @JsonProperty("head")
    private void unpackNestedHeadProperty(Map<String,Object> base) {
        this.branchName=(String)base.get("ref");

        Repository repoFromWhichPrOriginates=objectMapper.convertValue(base.get("repo"), Repository.class);
        isMadeFromForkedRepo=repoFromWhichPrOriginates.isFork();
    }

}
