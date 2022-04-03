package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.Event;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import lombok.extern.slf4j.Slf4j;

import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.PR_NUMBER;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.REPO;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.NOTIFICATION_FOR_NON_MERGEABLE_PR;
import static com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier.PULL_REQUEST;

@Slf4j
public class NotificationsHandler implements PushEventHandler {

    private final SourceControlEventsReactionPerformer gitHub;

    private final List<Notifier> notifiers;

    public NotificationsHandler(SourceControlEventsReactionPerformer gitHub, List<Notifier> notifiers) {

        this.gitHub = gitHub;
        this.notifiers = notifiers;

    }

    @Override
    public void handle(SourceControlEvent event, List<PullRequest> pullRequests) {

        PushEvent pushEvent;

        if (event instanceof PushEvent){
            pushEvent = (PushEvent) event;
        }
        else{
            log.warn("can't process the event as we are expecting a {}, but we got a {}",PushEvent.class,event.getClass());
            return;
        }

        pullRequests.stream()
                .filter(pr -> pr.getMergeable().equals(Boolean.FALSE))
                .forEach(pr ->
                            notifiers.forEach(n -> {
                                User user = User.buildFrom(pr, gitHub);

                                Event techEvent = Event.technical(NOTIFICATION_FOR_NON_MERGEABLE_PR);
                                techEvent.addAttribute(PR_NUMBER, String.valueOf(pr.getNumber()));
                                techEvent.addAttribute("pullRequestUrl", pr.getHtmlUrl());
                                techEvent.addAttribute(REPO, pr.getRepo().getFullName());
                                techEvent.addAttribute("pullRequestOwner", user.getLogin());
                                techEvent.publish();


                                Map<String,Object> additionalInfos=new HashMap<>();
                                additionalInfos.put(PULL_REQUEST,pr);

                                log.info("notifying that PR #{} is not mergeable..", pr.getNumber());
                                n.notify(user, Message.buildFromNotMergeablePR(pr, pushEvent),additionalInfos);
                            })
                );
    }
}
