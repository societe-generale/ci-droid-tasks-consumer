package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.Data;

@Data
public abstract class PullRequestEvent extends SourceControlEvent {

    private String action;

    private int prNumber;

}
