package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPushEvent implements PushEvent {

    private Repository repository;

    private String ref;

    @JsonProperty("head_commit")
    private com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.Commit headCommit;

    @Override
    public com.societegenerale.cidroid.tasks.consumer.services.model.Repository getRepository(){
        return repository.toStandardRepo().get();
    }

    @Override
    public com.societegenerale.cidroid.tasks.consumer.services.model.Commit getHeadCommit(){
        return headCommit.toStandardCommit();
    }

    String userEmail;

    String userName;

    String rawEvent;

    @Nonnull
    @Override
    public List<com.societegenerale.cidroid.tasks.consumer.services.model.Commit> getCommits() {

        if(commits==null){
            return Collections.emptyList();
        }

        return commits.stream().map(Commit::toStandardCommit).collect(Collectors.toList());
    }

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
