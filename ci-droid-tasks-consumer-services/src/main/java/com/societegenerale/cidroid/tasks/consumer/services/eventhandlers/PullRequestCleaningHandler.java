package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.PR_NUMBER;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.REPO;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.OLD_PR_CLOSED;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PullRequestCleaningHandler implements PushEventHandler {

    private final SourceControlEventsReactionPerformer remoteSourceControl;
    private final Supplier<LocalDateTime>  dateProvider;
    private final int prAgeLimitInDays;

    public PullRequestCleaningHandler(SourceControlEventsReactionPerformer remoteSourceControl,
                                      Supplier<LocalDateTime> dateProvider,
                                      int prAgeLimitInDays) {
        this.remoteSourceControl = remoteSourceControl;
        this.dateProvider = dateProvider;
        this.prAgeLimitInDays = prAgeLimitInDays;
    }

    @Override
    public void handle(SourceControlEvent event, List<PullRequest> pullRequests) {
        String repoFullName = event.getRepository().getFullName();

        pullRequests.stream()
                .filter(this::isPullRequestTooOld)
                .forEach(pullRequest -> closePullRequest(repoFullName, pullRequest));
    }

    private boolean isPullRequestTooOld(PullRequest pullRequest) {
        LocalDateTime creationDate = pullRequest.getCreationDate();
        LocalDateTime today = dateProvider.get();
        return creationDate.plusDays(prAgeLimitInDays).isBefore(today);
    }

    private void closePullRequest(String repoFullName, PullRequest pullRequest) {
        remoteSourceControl.closePullRequest(repoFullName, pullRequest.getNumber());

        Event techEvent = Event.technical(OLD_PR_CLOSED);
        techEvent.addAttribute(REPO, repoFullName);
        techEvent.addAttribute(PR_NUMBER, String.valueOf(pullRequest.getNumber()));
        techEvent.publish();
    }

}