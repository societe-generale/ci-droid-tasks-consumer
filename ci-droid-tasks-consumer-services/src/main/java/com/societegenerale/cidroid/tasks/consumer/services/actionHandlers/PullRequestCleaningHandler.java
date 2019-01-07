package com.societegenerale.cidroid.tasks.consumer.services.actionHandlers;

import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.model.DateProvider;
import com.societegenerale.cidroid.tasks.consumer.services.model.GitHubEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class PullRequestCleaningHandler implements PushEventOnDefaultBranchHandler {

    private RemoteGitHub remoteGitHub;
    private DateProvider dateProvider;

    private int prAgeLimitInDays;

    public PullRequestCleaningHandler(RemoteGitHub remoteGitHub,
                                      DateProvider dateProvider,
                                      int prAgeLimitInDays) {
        this.remoteGitHub = remoteGitHub;
        this.dateProvider = dateProvider;
        this.prAgeLimitInDays = prAgeLimitInDays;
    }

    @Override
    public void handle(GitHubEvent event, List<PullRequest> pullRequests) {
        String repoFullName = event.getRepository().getFullName();

        pullRequests.stream()
                .filter(this::isPullRequestTooOld)
                .forEach(pullRequest -> closePullRequest(repoFullName, pullRequest));
    }

    private boolean isPullRequestTooOld(PullRequest pullRequest) {
        LocalDateTime creationDate = pullRequest.getCreationDate();
        LocalDateTime today = dateProvider.now();
        return creationDate.plusDays(prAgeLimitInDays).isBefore(today);
    }

    private void closePullRequest(String repoFullName, PullRequest pullRequest) {
        remoteGitHub.closePullRequest(repoFullName, pullRequest.getNumber());
    }

}