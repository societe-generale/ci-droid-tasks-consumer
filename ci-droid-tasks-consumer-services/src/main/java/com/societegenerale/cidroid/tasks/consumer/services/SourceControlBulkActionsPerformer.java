package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;


/**
 * operations needed when performing bulk actions
 * Typically required when users push a bulk action (including their credentials) to CI-droid-tasks-consumer, so that it can be executed on their behalf
 */
public interface SourceControlBulkActionsPerformer extends RemoteSourceControl{

    ResourceContent fetchContent(String repoFullName, String path, String branch);

    UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String oauthToken)
            throws RemoteSourceControlAuthorizationException;

    UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String oauthToken)
            throws RemoteSourceControlAuthorizationException;

    PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String oauthToken)
            throws RemoteSourceControlAuthorizationException;

    Reference fetchHeadReferenceFrom(String repoFullName, String branchName);

    /**
     * It's important that implementations throw correctly the expected exceptions, especially BranchAlreadyExistsException, as we often rely on this to know if we should proceed :
     * in the case of an action creating a pull request with several fails, it's normal that the branch already exists
     * @param repoFullName
     * @param branchName
     * @param fromReferenceSha1
     * @param oauthToken
     * @return
     * @throws BranchAlreadyExistsException
     * @throws RemoteSourceControlAuthorizationException
     */
    Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String oauthToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException;

    Optional<Repository> fetchRepository(String repoFullName);

    @Nonnull
    List<PullRequest> fetchOpenPullRequests(String repoFullName);

    User fetchCurrentUser(String oAuthToken);

    PullRequest fetchPullRequestDetails(String repoFullName, int prNumber);
}


