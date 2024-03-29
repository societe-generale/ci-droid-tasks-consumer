package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequestEvent implements PullRequestEvent {

    private String action;

    private Repository repository;

    @JsonProperty("number")
    private int prNumber;

    @Override
    public com.societegenerale.cidroid.tasks.consumer.services.model.Repository getRepository(){
        return repository.toStandardRepo().get();
    }

    @Override
    public void setRawEvent(String rawEvent) {

    }
}
