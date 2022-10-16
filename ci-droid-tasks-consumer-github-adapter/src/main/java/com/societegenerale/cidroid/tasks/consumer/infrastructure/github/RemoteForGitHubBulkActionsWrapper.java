package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
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

import static java.util.stream.Collectors.toList;

public class RemoteForGitHubBulkActionsWrapper implements SourceControlBulkActionsPerformer {

    private final FeignRemoteForGitHubBulkActions feignRemoteForGitHubBulkActions;

    public RemoteForGitHubBulkActionsWrapper(
            FeignRemoteForGitHubBulkActions feignRemoteForGitHubBulkActions) {

        this.feignRemoteForGitHubBulkActions = feignRemoteForGitHubBulkActions;
    }

    @Override
    public ResourceContent fetchContent(String repoFullName, String path, String branch) {

        var gitHubResourceContent=feignRemoteForGitHubBulkActions.fetchContent(repoFullName,path,branch);

        return gitHubResourceContent.toStandardResourceContent();
    }

    @Override
    public UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var gitHubDirectCommit=com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.DirectCommit.from(directCommit);

        var gitHubUpdatedResource=feignRemoteForGitHubBulkActions.updateContent(repoFullName,path,gitHubDirectCommit,sourceControlAccessToken);

        return gitHubUpdatedResource.toStandardUpdatedResource();
    }

    @Override
    public UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var gitHubDirectCommit=com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.DirectCommit.from(directCommit);

        var gitHubDeletedResource=feignRemoteForGitHubBulkActions.deleteContent(repoFullName,path,gitHubDirectCommit,sourceControlAccessToken);

        return gitHubDeletedResource.toStandardUpdatedResource();
    }

    @Override
    public PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var gitHubPrToCreate=com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.PullRequestToCreate.from(newPr);

        var gitHubPr=feignRemoteForGitHubBulkActions.createPullRequest(repoFullName, gitHubPrToCreate, sourceControlAccessToken);

        return gitHubPr.toStandardPullRequest();
    }

    @Override
    public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {

        var gitHubReference=feignRemoteForGitHubBulkActions.fetchHeadReferenceFrom(repoFullName, branchName);

        return gitHubReference.toStandardReference();
    }

    @Override
    public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlAccessToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        var gitHubReference=feignRemoteForGitHubBulkActions.createBranch(repoFullName, branchName,fromReferenceSha1,sourceControlAccessToken);

        return gitHubReference.toStandardReference();
    }

    @Override
    public Optional<Repository> fetchRepository(String repoFullName) {

        var gitHubRepo=feignRemoteForGitHubBulkActions.fetchRepository(repoFullName);

        if(gitHubRepo.isEmpty()){
            return Optional.empty();
        }

        return gitHubRepo.get().toStandardRepo();
    }

    @Nonnull
    @Override
    public List<PullRequest> fetchOpenPullRequests(String repoFullName) {

        var githubOpenPrs= feignRemoteForGitHubBulkActions.fetchOpenPullRequests(repoFullName);

        return githubOpenPrs.stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.PullRequest::toStandardPullRequest)
                .collect(toList());
    }

    @Override
    public User fetchCurrentUser(String sourceControlAccessToken, String emailAddress) {
        return feignRemoteForGitHubBulkActions.fetchCurrentUser(sourceControlAccessToken,emailAddress).toStandardUser();
    }
}
