package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;

import java.util.List;

public class PushEventMonitoringHandler implements PushEventHandler {


    @Override
    public void handle(SourceControlEvent event, List<PullRequest> pullRequests) {

    }
}
