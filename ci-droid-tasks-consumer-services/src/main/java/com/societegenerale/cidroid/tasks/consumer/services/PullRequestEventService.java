package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.Event;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.List;

import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.PR_NUMBER;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.REPO;

@Slf4j
public class PullRequestEventService {

    private static final List<String> acceptedPullRequestEventType = Arrays.asList("opened", "synchronize");

    private List<PullRequestEventHandler> actionHandlers;

    public PullRequestEventService(List<PullRequestEventHandler> actionHandlers) {
        this.actionHandlers = actionHandlers;
    }

    public void onGitHubPullRequestEvent(PullRequestEvent pullRequestEvent) {

        if (!acceptedPullRequestEventType.contains(pullRequestEvent.getAction())) {
            log.debug("not processing pullRequest event of type {}", pullRequestEvent.getAction());
            return;
        }

        Event techEvent = Event.technical(MonitoringEvents.PULL_REQUEST_EVENT_TO_PROCESS);
        techEvent.addAttribute(REPO, pullRequestEvent.getRepository().getFullName());
        techEvent.addAttribute(PR_NUMBER, String.valueOf(pullRequestEvent.getPrNumber()));

        StopWatch stopWatch = StopWatch.createStarted();

        for (PullRequestEventHandler pullRequestEventHandler : actionHandlers) {
            pullRequestEventHandler.handle(pullRequestEvent);
        }

        stopWatch.stop();
        techEvent.addAttribute("processTime", String.valueOf(stopWatch.getTime()));
        techEvent.publish();



    }

}
