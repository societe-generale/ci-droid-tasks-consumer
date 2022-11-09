package com.societegenerale.cidroid.tasks.consumer.services;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;


/**
 * operations needed when performing bulk actions
 * Typically required when users push a bulk action (including their credentials) to CI-droid-tasks-consumer, so that it can be executed on their behalf
 */
public interface SourceControlBulkActionsPerformer extends RemoteSourceControl{

    ResourceContent fetchContent(String repoFullName, String path, String branch);

    UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException;

    UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException;

    PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException;

    Reference fetchHeadReferenceFrom(String repoFullName, String branchName);

    /**
     * It's important that implementations throw correctly the expected exceptions, especially BranchAlreadyExistsException, as we often rely on this to know if we should proceed :
     * in the case of an action creating a pull request with several fails, it's normal that the branch already exists
     * @param repoFullName
     * @param branchName
     * @param fromReferenceSha1
     * @param sourceControlAccessToken
     * @return
     * @throws BranchAlreadyExistsException
     * @throws RemoteSourceControlAuthorizationException
     */
    Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlAccessToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException;

    Optional<Repository> fetchRepository(String repoFullName);

    @Nonnull
    List<PullRequest> fetchOpenPullRequests(String repoFullName);

    /**
     * in most cases, providing the personal access token is enough. but some providers (like Azure devops) don't allow to retrieve the user details from the token.
     * In that case, we'll use the email address / login to build the User we need.
     * Therefore, each implementation may use any of the parameters to fetch a User from the given system.
     * @param sourceControlAccessToken
     * @param emailAddress
     * @param login
     * @return a user, as defined in the particular source control system
     */
    User fetchCurrentUser(String sourceControlAccessToken, String emailAddress, String login);

}


