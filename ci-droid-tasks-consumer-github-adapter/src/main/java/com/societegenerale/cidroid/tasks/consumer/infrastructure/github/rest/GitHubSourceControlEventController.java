package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.rest;

import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/cidroid-sync-webhook")
@Slf4j
public class GitHubSourceControlEventController {

    private final PushEventService pushEventService;

    private final PullRequestEventService pullRequestEventService;

    private final SourceControlEventMapper sourceControlEventMapper;

    public GitHubSourceControlEventController(PullRequestEventService pullRequestEventService, PushEventService pushEventService, SourceControlEventMapper sourceControlEventMapper) {
        this.pullRequestEventService = pullRequestEventService;
        this.pushEventService = pushEventService;
        this.sourceControlEventMapper=sourceControlEventMapper;
    }


    @PostMapping(value="/github",headers = "X-Github-Event=push")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> onGitHubPushEvent(HttpEntity<String> rawPushEvent) {
        return processPushEvent(rawPushEvent);
    }


    private ResponseEntity<?> processPushEvent(HttpEntity<String> rawPushEvent) {
        PushEvent pushEvent=null;
        try {
            pushEvent=sourceControlEventMapper.deserializePushEvent(rawPushEvent.getBody());

            log.info("received event on branch {} for repo {}", pushEvent.getRef(), pushEvent.getRepository().getFullName());

            if(pushEvent.happenedOnDefaultBranch()) {
                pushEventService.onPushOnDefaultBranchEvent(pushEvent);
            }
            else{
                pushEventService.onPushOnNonDefaultBranchEvent(pushEvent);
            }

            return ResponseEntity.accepted().build();

        } catch (Exception e) {
            log.warn("problem while processing the event {}", pushEvent, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(path="/github",headers = "X-Github-Event=pull_request")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> onGitHubPullRequestEvent(HttpEntity<String> rawPullRequestEvent) {

        return processPullRequestEvent(rawPullRequestEvent);
    }

    private ResponseEntity<?> processPullRequestEvent(HttpEntity<String> rawPullRequestEvent) {

        PullRequestEvent pullRequestEvent=null;

        try {
            pullRequestEvent = sourceControlEventMapper.deserializePullRequestEvent(rawPullRequestEvent.getBody());

            log.info("received pullRequest event of type {} for repo {}", pullRequestEvent.getAction(), pullRequestEvent.getRepository().getFullName());

            pullRequestEventService.onPullRequestEvent(pullRequestEvent);

            return ResponseEntity.accepted().build();
        }
        catch (Exception e) {
            log.warn("problem while processing the event {}",pullRequestEvent, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
