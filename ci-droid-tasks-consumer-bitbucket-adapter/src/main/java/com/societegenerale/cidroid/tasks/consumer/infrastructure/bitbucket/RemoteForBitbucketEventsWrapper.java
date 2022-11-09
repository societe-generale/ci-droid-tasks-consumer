package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.*;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class RemoteForBitbucketEventsWrapper implements SourceControlEventsReactionPerformer {

    private final FeignRemoteForBitbucketEvents feignRemoteForBitbucketEvents;

    public RemoteForBitbucketEventsWrapper(FeignRemoteForBitbucketEvents feignRemoteForBitbucketEvents) {
        this.feignRemoteForBitbucketEvents = feignRemoteForBitbucketEvents;
    }

    @Nonnull
    @Override
    public List<PullRequest> fetchOpenPullRequests(String repoFullName) {
        return feignRemoteForBitbucketEvents.fetchOpenPullRequests(repoFullName).getValues()
                .stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequest::toStandardPullRequest)
                .collect(toList());
    }

    @Override
    public PullRequest fetchPullRequestDetails(String repoFullName, int prNumber) {
        return feignRemoteForBitbucketEvents.fetchPullRequestDetails(repoFullName, prNumber).toStandardPullRequest();
    }

    @Override
    public User fetchUser(String login) {
        return feignRemoteForBitbucketEvents.fetchUser(login).toStandardUser();
    }

    @Override
    public void addCommentOnPR(String repoFullName, int prNumber, Comment comment) {
        feignRemoteForBitbucketEvents.addCommentOnPR(repoFullName, prNumber, new com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.Comment(comment.getBody()));
    }

    @Nonnull
    @Override
    public List<PullRequestFile> fetchPullRequestFiles(String repoFullName, int prNumber) {
        return feignRemoteForBitbucketEvents.fetchPullRequestFiles(repoFullName, prNumber).getValues()
                .stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequestFile::toStandardPullRequestFile)
                .collect(toList());
    }

    @Nonnull
    @Override
    public List<PullRequestComment> fetchPullRequestComments(String repoFullName, int prNumber) {
        return feignRemoteForBitbucketEvents.fetchPullRequestComments(repoFullName, prNumber).getValues()
                .stream()
                .filter(it -> "COMMENTED".equals(it.getCommentAction()))
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequestComment::toStandardPullRequestComment)
                .collect(toList());
    }

    @Override
    public void closePullRequest(String repoFullName, int prNumber) {
        feignRemoteForBitbucketEvents.closePullRequest(repoFullName, prNumber);
    }
}
