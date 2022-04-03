package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import java.util.List;

import javax.annotation.Nonnull;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;

import static java.util.stream.Collectors.toList;

public class RemoteForGitHubEventsWrapper implements SourceControlEventsReactionPerformer {

    private final FeignRemoteForGitHubEvents feignRemoteForGitHubEvents;

    public RemoteForGitHubEventsWrapper(FeignRemoteForGitHubEvents feignRemoteForGitHubEvents) {
        this.feignRemoteForGitHubEvents = feignRemoteForGitHubEvents;
    }

    @Nonnull
    @Override
    public List<PullRequest> fetchOpenPullRequests(String repoFullName) {
        return feignRemoteForGitHubEvents.fetchOpenPullRequests(repoFullName)
                .stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.PullRequest::toStandardPullRequest)
                .collect(toList());
    }

    @Override
    public PullRequest fetchPullRequestDetails(String repoFullName, int prNumber) {
        return feignRemoteForGitHubEvents.fetchPullRequestDetails(repoFullName, prNumber).toStandardPullRequest();
    }

    @Override
    public User fetchUser(String login) {
        return feignRemoteForGitHubEvents.fetchUser(login).toStandardUser();
    }

    @Override
    public void addCommentOnPR(String repoFullName, int prNumber, Comment comment) {
        feignRemoteForGitHubEvents.addCommentOnPR(repoFullName, prNumber, new com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.Comment(comment.getBody()));
    }

    @Nonnull
    @Override
    public List<PullRequestFile> fetchPullRequestFiles(String repoFullName, int prNumber) {
        return feignRemoteForGitHubEvents.fetchPullRequestFiles(repoFullName, prNumber)
                .stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.PullRequestFile::toStandardPullRequestFile)
                .collect(toList());
    }

    @Nonnull
    @Override
    public List<PullRequestComment> fetchPullRequestComments(String repoFullName, int prNumber) {
        return feignRemoteForGitHubEvents.fetchPullRequestComments(repoFullName, prNumber)
                .stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.PullRequestComment::toStandardPullRequestComment)
                .collect(toList());
    }

    @Override
    public void closePullRequest(String repoFullName, int prNumber) {
        feignRemoteForGitHubEvents.closePullRequest(repoFullName, prNumber);
    }
}
