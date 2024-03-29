package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PullRequestComment {

    @JsonProperty("body")
    private String comment;

    @JsonProperty("user")
    private User author;

    public com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestComment toStandardPullRequestComment(){

        return new com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestComment(comment,author.toStandardUser());

    }

}
