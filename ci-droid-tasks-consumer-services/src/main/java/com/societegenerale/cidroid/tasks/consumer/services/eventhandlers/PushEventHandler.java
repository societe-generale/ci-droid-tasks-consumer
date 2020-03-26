package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;

import java.util.List;

public interface PushEventHandler {

    void handle(SourceControlEvent event, List<PullRequest> pullRequests);

}
