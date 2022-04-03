package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import lombok.Builder;

@Builder
public class TestPullRequestEvent implements PullRequestEvent {

    private final String action;
    private final int prNumber;
    private final Repository repository;

    private String rawEvent;

    public TestPullRequestEvent(String action, int prNumber, Repository repository,String rawEvent) {
        this.action=action;
        this.prNumber=prNumber;
        this.repository=repository;
        this.rawEvent=rawEvent;
    }

    @Override
    public String getAction() {
        return action;
    }


    @Override
    public int getPrNumber() {
        return prNumber;
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public void setRawEvent(String rawEvent) {
        this.rawEvent=rawEvent;
    }
}
