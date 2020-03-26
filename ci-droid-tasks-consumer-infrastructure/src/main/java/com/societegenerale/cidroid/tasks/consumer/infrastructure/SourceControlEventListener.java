package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventOnDefaultBranchService;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SourceControlEventListener {

    private PushEventOnDefaultBranchService pushOnDefaultBranchService;

    private PullRequestEventService pullRequestEventService;

    public SourceControlEventListener(PushEventOnDefaultBranchService pushOnDefaultBranchService, PullRequestEventService pullRequestEventService) {
        this.pushOnDefaultBranchService = pushOnDefaultBranchService;
        this.pullRequestEventService = pullRequestEventService;

    }

    public void onPushEventOnDefaultBranch(PushEvent pushEvent) {

        try {
            log.info("received event on branch {} for repo {}", pushEvent.getRef(), pushEvent.getRepository().getFullName());

            pushOnDefaultBranchService.onPushEvent(pushEvent);
        } catch (Exception e) {
            log.warn("problem while processing the event {}", pushEvent, e);
        }
    }

    public void onPullRequestEvent(PullRequestEvent pullRequestEvent) {

        log.info("received pullRequest event of type {} for repo {}",pullRequestEvent.getAction(),pullRequestEvent.getRepository().getFullName());

        pullRequestEventService.onPullRequestEvent(pullRequestEvent);

    }

}
