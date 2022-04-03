package com.societegenerale.cidroid.tasks.consumer.services.model;

public interface PullRequestEvent extends SourceControlEvent {

    String getAction();

    int getPrNumber();

}
