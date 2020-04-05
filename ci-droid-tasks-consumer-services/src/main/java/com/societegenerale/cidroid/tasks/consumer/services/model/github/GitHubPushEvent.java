package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
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

    List<Commit> commits;

    @JsonProperty("pusher")
    private void unpackNestedPusher(Map<String, Object> pusher) {
        this.userName = (String) pusher.get("name");
        this.userEmail = (String) pusher.get("email");
    }

    @Override
    public int getNbCommits() {
        throw new NotImplementedException("need to provide the json mapping for GitHub");
    }


}
