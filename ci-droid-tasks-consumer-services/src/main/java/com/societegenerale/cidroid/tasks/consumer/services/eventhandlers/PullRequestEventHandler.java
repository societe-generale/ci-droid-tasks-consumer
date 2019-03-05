package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestEvent;

public interface PullRequestEventHandler {

    void handle(PullRequestEvent event);

}
