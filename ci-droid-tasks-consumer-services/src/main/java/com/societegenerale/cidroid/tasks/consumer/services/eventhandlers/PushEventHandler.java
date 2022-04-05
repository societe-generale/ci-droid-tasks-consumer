package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.util.List;

import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;

public interface PushEventHandler {

    void handle(SourceControlEvent event, List<PullRequest> pullRequests);

}
