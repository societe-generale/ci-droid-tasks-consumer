package com.societegenerale.cidroid.tasks.consumer.services;

import java.util.List;

import javax.annotation.Nonnull;

import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.Event;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.REPO;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.PUSH_EVENT_TO_PROCESS;
import static java.util.stream.Collectors.toList;

@Slf4j
public class PushEventService {

    private final SourceControlEventsReactionPerformer gitHub;

    private final List<PushEventHandler> defaultBranchPushActionHandlers;

    private final PushEventMonitor pushEventMonitor;

    private final boolean enableMonitoring;

    @Setter
    private long sleepDurationBeforeTryingAgainToFetchMergeableStatus = 300;

    @Setter
    private int maxRetriesForMergeableStatus = 10;

    public PushEventService(SourceControlEventsReactionPerformer gitHub, List<PushEventHandler> pushEventHandlers, boolean enablePushEventsMonitoring, PushEventMonitor pushEventMonitor) {

        if(enablePushEventsMonitoring && pushEventMonitor == null){
            throw new IllegalStateException("if Push events monitoring is enabled, then a proper "+PushEventMonitor.class+" must be provided");
        }

        this.gitHub = gitHub;
        this.defaultBranchPushActionHandlers = pushEventHandlers;

        this.pushEventMonitor = pushEventMonitor;
        this.enableMonitoring=enablePushEventsMonitoring;
    }

    public void onPushOnNonDefaultBranchEvent(PushEvent pushEvent) {
        monitorEvent(pushEvent);
    }

    public void onPushOnDefaultBranchEvent(PushEvent pushEvent) {

        if (shouldNotProcess(pushEvent)) {
            return;
        }

        monitorEvent(pushEvent);

        Event techEvent = Event.technical(PUSH_EVENT_TO_PROCESS);
        techEvent.addAttribute(REPO, pushEvent.getRepository().getFullName());

        StopWatch stopWatch = StopWatch.createStarted();

        List<PullRequest> openPRs = retrieveOpenPrs(pushEvent.getRepository().getFullName());

        List<PullRequest> openPRsWithDefinedMergeabilityStatus = figureOutMergeableStatusFor(openPRs, 0);

        if (log.isInfoEnabled()) {
            logPrMergeabilityStatus(openPRsWithDefinedMergeabilityStatus);
        }

        for (PushEventHandler pushEventHandler : defaultBranchPushActionHandlers) {

            try {
                pushEventHandler.handle(pushEvent, openPRsWithDefinedMergeabilityStatus);
            } catch (RuntimeException e) {
                log.warn("exception thrown during event handling by " + pushEventHandler.getClass(), e);
            }
        }

        stopWatch.stop();
        techEvent.addAttribute("processTime", String.valueOf(stopWatch.getTime()));
        techEvent.publish();

    }

    private boolean shouldNotProcess(PushEvent pushEvent) {

        //TODO now that we receive non default branch events on a separate channel, maybe this is not required anymore..
        if (!pushEvent.happenedOnDefaultBranch()) {
            log.warn("received an event from branch that is not default, ie {} - how is it possible ? ", pushEvent.getRef());
            return true;
        }

        return false;
    }

    private void logPrMergeabilityStatus(List<PullRequest> openPRsWithDefinedMergeabilityStatus) {
        if (!openPRsWithDefinedMergeabilityStatus.isEmpty()) {

            StringBuilder sb = new StringBuilder("PR status :\n");

            for (PullRequest pr : openPRsWithDefinedMergeabilityStatus) {
                sb.append("\t- PR #").append(pr.getNumber()).append(" : ").append(pr.getMergeStatus()).append("\n");
            }

            log.info(sb.toString());
        }
    }

    @Nonnull
    private List<PullRequest> figureOutMergeableStatusFor(List<PullRequest> openPRs, int nbRetry) {

        List<PullRequest> pullRequestsWithDefinedMergeabilityStatus = openPRs.stream()
                .filter(pr -> PRmergeableStatus.UNKNOWN != pr.getMergeStatus())
                .collect(toList());

        List<PullRequest> pullRequestsWithUnknownMergeabilityStatus = openPRs.stream()
                .filter(pr -> PRmergeableStatus.UNKNOWN == pr.getMergeStatus())
                .collect(toList());

        if (!pullRequestsWithUnknownMergeabilityStatus.isEmpty() && nbRetry < maxRetriesForMergeableStatus) {

            if (log.isDebugEnabled()) {

                StringBuilder sb = new StringBuilder("these PRs don't have a mergeable status yet :\n");

                pullRequestsWithUnknownMergeabilityStatus
                        .forEach(pr -> sb.append("\t - ").append(pr.getNumber()).append("\n"));

                sb.append("waiting for ")
                        .append(sleepDurationBeforeTryingAgainToFetchMergeableStatus)
                        .append("ms before trying again for the ")
                        .append(nbRetry + 1)
                        .append("th time...");

                log.debug(sb.toString());
            }

            try {
                Thread.sleep(sleepDurationBeforeTryingAgainToFetchMergeableStatus);
            } catch (InterruptedException e) {
                log.error("interrupted while sleeping to get PR status", e);
            }

            List<PullRequest> prsWithUpdatedStatus = pullRequestsWithUnknownMergeabilityStatus.stream()
                    .map(pr -> gitHub.fetchPullRequestDetails(pr.getRepo().getFullName(), pr.getNumber()))
                    .collect(toList());

            pullRequestsWithDefinedMergeabilityStatus.addAll(figureOutMergeableStatusFor(prsWithUpdatedStatus, ++nbRetry));
        } else if (nbRetry >= maxRetriesForMergeableStatus) {

            log.warn("not able to retrieve merge status for below PRs after several tries.. giving up");
            pullRequestsWithUnknownMergeabilityStatus
                    .forEach(pr -> log.info("\t - {}", pr.getNumber()));
        }

        return pullRequestsWithDefinedMergeabilityStatus;
    }

    @Nonnull
    private List<PullRequest> retrieveOpenPrs(String repoFullName) {

        List<PullRequest> openPrs = gitHub.fetchOpenPullRequests(repoFullName);

        log.info("{} open PRs found on repo {}", openPrs.size(), repoFullName);

        return openPrs.stream()
                .map(pr -> gitHub.fetchPullRequestDetails(repoFullName, pr.getNumber()))
                .collect(toList());
    }


    private void monitorEvent(PushEvent pushEvent) {

        if(!enableMonitoring){
            return;
        }

        pushEventMonitor.record(pushEvent);
    }

}
