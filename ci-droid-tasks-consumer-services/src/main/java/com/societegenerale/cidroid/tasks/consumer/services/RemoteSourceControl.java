package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.*;

import java.util.List;
import java.util.Optional;

public interface RemoteSourceControl {

    List<PullRequest> fetchOpenPullRequests(String repoFullName);

    PullRequest fetchPullRequestDetails(String repoFullName, int prNumber);

    User fetchUser(String login);

    User fetchCurrentUser(String oAuthToken);

    void addCommentOnPR(String repoFullName,
            int prNumber,
            Comment comment);

    List<PullRequestFile> fetchPullRequestFiles(String repoFullName, int prNumber);

    List<PullRequestComment> fetchPullRequestComments(String repoFullName, int prNumber);

    ResourceContent fetchContent(String repoFullName, String path, String branch);

    UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String oauthToken)
            throws RemoteSourceControlAuthorizationException;

    UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String oauthToken)
            throws RemoteSourceControlAuthorizationException;

    PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String oauthToken)
            throws RemoteSourceControlAuthorizationException;

    void closePullRequest(String repoFullName, int prNumber);

    Optional<Repository> fetchRepository(String repoFullName);

    Reference fetchHeadReferenceFrom(String repoFullName, String branchName);

    Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String oauthToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException;

}


