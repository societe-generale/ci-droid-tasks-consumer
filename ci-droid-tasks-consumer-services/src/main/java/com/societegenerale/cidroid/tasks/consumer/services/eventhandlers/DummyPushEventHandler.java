package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DummyPushEventHandler implements PushEventHandler {

    @Override
    public void handle(SourceControlEvent event, List<PullRequest> pullRequests) {
        log.info("event handled by dummy handler : {}",event);
    }
}
