package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;


/**
 * operations needed when reacting to source control events (code push).
 * Typically required when CI-droid-tasks-consumer is running as a webhook endpoint to the source control
 */
public interface SourceControlEventsReactionPerformer {

    @Nonnull
    List<PullRequest> fetchOpenPullRequests(String repoFullName);

    PullRequest fetchPullRequestDetails(String repoFullName, int prNumber);

    User fetchUser(String login);

    User fetchCurrentUser(String oAuthToken);

    void addCommentOnPR(String repoFullName,
            int prNumber,
            Comment comment);

    @Nonnull
    List<PullRequestFile> fetchPullRequestFiles(String repoFullName, int prNumber);

    @Nonnull
    List<PullRequestComment> fetchPullRequestComments(String repoFullName, int prNumber);

    void closePullRequest(String repoFullName, int prNumber);

    Optional<Repository> fetchRepository(String repoFullName);

    Reference fetchHeadReferenceFrom(String repoFullName, String branchName);

}


