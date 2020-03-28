package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SourceControlEventListener {

    private PushEventService pushOnDefaultBranchService;

    private PushEventService pushOnNonDefaultBranchService;

    private PullRequestEventService pullRequestEventService;

    public SourceControlEventListener(PushEventService pushOnDefaultBranchService, PullRequestEventService pullRequestEventService,PushEventService pushOnNonDefaultBranchService) {
        this.pushOnDefaultBranchService = pushOnDefaultBranchService;
        this.pullRequestEventService = pullRequestEventService;
        this.pushOnNonDefaultBranchService = pushOnNonDefaultBranchService;
    }

    public void onPushEventOnDefaultBranch(PushEvent pushEvent) {

        try {
            log.info("received event on branch {} for repo {}", pushEvent.getRef(), pushEvent.getRepository().getFullName());

            pushOnDefaultBranchService.onPushOnDefaultBranchEvent(pushEvent);
        } catch (Exception e) {
            log.warn("problem while processing the event {}", pushEvent, e);
        }
    }

    public void onPushEventOnNonDefaultBranch(PushEvent pushEvent) {
        try {
            log.info("received event on branch {} for repo {}", pushEvent.getRef(), pushEvent.getRepository().getFullName());

            pushOnNonDefaultBranchService.onPushOnNonDefaultBranchEvent(pushEvent);
        } catch (Exception e) {
            log.warn("problem while processing the event {}", pushEvent, e);
        }
    }

    public void onPullRequestEvent(PullRequestEvent pullRequestEvent) {

        log.info("received pullRequest event of type {} for repo {}",pullRequestEvent.getAction(),pullRequestEvent.getRepository().getFullName());

        pullRequestEventService.onPullRequestEvent(pullRequestEvent);

    }

}
