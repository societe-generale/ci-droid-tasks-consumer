package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPushEvent implements PushEvent {

    String rawEvent;
    private Repository repository;
    private String ref;
    private User actor;
    @Nonnull
    private List<Change> changes;

    @Override
    public com.societegenerale.cidroid.tasks.consumer.services.model.Repository getRepository() {
        return repository.toStandardRepo().get();
    }

    public String getRef() {
        return changes.stream().findFirst().get().getRefId();
    }

    @Override
    public com.societegenerale.cidroid.tasks.consumer.services.model.Commit getHeadCommit() {
        Change change = changes.stream().findFirst().get();
        return com.societegenerale.cidroid.tasks.consumer.services.model.Commit.builder().id(change.getToHash())
                .author(actor.toStandardUser()).build();
    }

    public String getUserEmail() {
        return actor.getEmail();
    }

    public String getUserName() {
        return actor.getLogin();
    }

    @Nonnull
    @Override
    public List<com.societegenerale.cidroid.tasks.consumer.services.model.Commit> getCommits() {

        var user = actor.toStandardUser();
        return changes.stream().map(it -> com.societegenerale.cidroid.tasks.consumer.services.model.Commit.builder().id(it.getToHash())
                .author(user).build()).collect(Collectors.toList());
    }

    @Override
    public int getNbCommits() {
        throw new NotImplementedException("need to provide the json mapping for Bitbucket");
    }


}
