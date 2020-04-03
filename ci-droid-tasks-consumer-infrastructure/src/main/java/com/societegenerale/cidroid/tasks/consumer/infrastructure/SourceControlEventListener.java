package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SourceControlEventListener {

    private PushEventService pushEventService;

    private PullRequestEventService pullRequestEventService;

    private  SourceControlEventMapper eventMapper;

    public SourceControlEventListener(PullRequestEventService pullRequestEventService,PushEventService pushEventService, SourceControlEventMapper eventMapper) {
        this.pullRequestEventService = pullRequestEventService;
        this.pushEventService = pushEventService;
        this.eventMapper=eventMapper;
    }

    public void onPushEventOnDefaultBranch(String rawPushEvent) {

        PushEvent pushEvent=null;
        try {
            pushEvent=eventMapper.deserializePushEvent(rawPushEvent);

            log.info("received event on branch {} for repo {}", pushEvent.getRef(), pushEvent.getRepository().getFullName());

            pushEventService.onPushOnDefaultBranchEvent(pushEvent);
        } catch (Exception e) {
            log.warn("problem while processing the event {}", pushEvent, e);
        }
    }

    public void onPushEventOnNonDefaultBranch(String rawPushEvent) {

        PushEvent pushEvent=null;
        try {
            pushEvent=eventMapper.deserializePushEvent(rawPushEvent);

            log.info("received event on branch {} for repo {}", pushEvent.getRef(), pushEvent.getRepository().getFullName());

            pushEventService.onPushOnNonDefaultBranchEvent(pushEvent);
        } catch (Exception e) {
            log.warn("problem while processing the event {}", pushEvent, e);
        }
    }

    public void onPullRequestEvent(String rawPullRequestEvent) {

        PullRequestEvent pullRequestEvent=null;

        try {
            pullRequestEvent = eventMapper.deserializePullRequestEvent(rawPullRequestEvent);

            log.info("received pullRequest event of type {} for repo {}", pullRequestEvent.getAction(), pullRequestEvent.getRepository().getFullName());

            pullRequestEventService.onPullRequestEvent(pullRequestEvent);
        }
        catch (Exception e) {
                log.warn("problem while processing the event {}",pullRequestEvent, e);
            }
    }

}
