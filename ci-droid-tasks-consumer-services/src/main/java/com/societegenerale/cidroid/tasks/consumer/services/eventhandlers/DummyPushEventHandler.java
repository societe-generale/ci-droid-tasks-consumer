package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.util.List;

import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DummyPushEventHandler implements PushEventHandler {

    @Override
    public void handle(SourceControlEvent event, List<PullRequest> pullRequests) {
        log.info("event handled by dummy handler : {}",event);
    }
}
