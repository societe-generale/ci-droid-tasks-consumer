package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.societegenerale.cidroid.tasks.consumer.services.model.GitHubEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;

import java.util.List;

public interface PushEventOnDefaultBranchHandler {

    void handle(GitHubEvent event, List<PullRequest> pullRequests);

}
