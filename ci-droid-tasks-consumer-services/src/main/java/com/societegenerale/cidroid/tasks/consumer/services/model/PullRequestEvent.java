package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import lombok.Data;

@Data
public abstract class PullRequestEvent implements SourceControlEvent {

    private String action;

    private int prNumber;

    private Repository repository;

    @Override
    public Repository getRepository() {
        return repository;
    }
}
