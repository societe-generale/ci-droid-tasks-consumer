package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPushEvent extends PushEvent {

    private Repository repository;

    private String ref;

    @JsonProperty("head_commit")
    private Commit headCommit;

    String userEmail;

    String userName;

    @JsonProperty("pusher")
    private void unpackNestedPusher(Map<String,Object> pusher) {
        this.userName = (String)pusher.get("name");
        this.userEmail = (String)pusher.get("email");
    }

}

