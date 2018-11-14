package com.societegenerale.cidroid.tasks.consumer.services.actionHandlers;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DummyPullRequestEventHandler implements PullRequestEventHandler {

    @Override
    public void handle(PullRequestEvent event) {
      log.info("event handled by dummy handler : {}",event);
    }
}
