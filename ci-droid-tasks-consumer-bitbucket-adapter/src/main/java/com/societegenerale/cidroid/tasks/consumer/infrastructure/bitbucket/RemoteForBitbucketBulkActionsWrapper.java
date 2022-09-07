package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class RemoteForBitbucketBulkActionsWrapper implements SourceControlBulkActionsPerformer {

    private final FeignRemoteForBitbucketBulkActions feignRemoteForBitbucketBulkActions;

    public RemoteForBitbucketBulkActionsWrapper(
            FeignRemoteForBitbucketBulkActions feignRemoteForBitbucketBulkActions) {

        this.feignRemoteForBitbucketBulkActions = feignRemoteForBitbucketBulkActions;
    }

    @Override
    public ResourceContent fetchContent(String repoFullName, String path, String branch) {

        var bitbucketResourceContent=feignRemoteForBitbucketBulkActions.fetchContent(repoFullName,path,branch);

        return bitbucketResourceContent.toStandardResourceContent();
    }

    @Override
    public UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var bitbucketDirectCommit=com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.DirectCommit.from(directCommit);

        var bitbucketUpdatedResource=feignRemoteForBitbucketBulkActions.updateContent(repoFullName,path,bitbucketDirectCommit,sourceControlAccessToken);

        return bitbucketUpdatedResource.toStandardUpdatedResource();
    }

    @Override
    public UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var bitbucketDirectCommit=com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.DirectCommit.from(directCommit);

        var bitbucketDeletedResource=feignRemoteForBitbucketBulkActions.deleteContent(repoFullName,path,bitbucketDirectCommit,sourceControlAccessToken);

        return bitbucketDeletedResource.toStandardUpdatedResource();
    }

    @Override
    public PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var bitbucketPrToCreate=com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequestToCreate.from(newPr);

        var bitbucketPr=feignRemoteForBitbucketBulkActions.createPullRequest(repoFullName, bitbucketPrToCreate, sourceControlAccessToken);

        return bitbucketPr.toStandardPullRequest();
    }

    @Override
    public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {

        var bitbucketReference=feignRemoteForBitbucketBulkActions.fetchHeadReferenceFrom(repoFullName, branchName);

        return bitbucketReference.toStandardReference();
    }

    @Override
    public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlAccessToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        var bitbucketReference=feignRemoteForBitbucketBulkActions.createBranch(repoFullName, branchName,fromReferenceSha1,sourceControlAccessToken);

        return bitbucketReference.toStandardReference();
    }

    @Override
    public Optional<Repository> fetchRepository(String repoFullName) {

        var bitbucketRepo=feignRemoteForBitbucketBulkActions.fetchRepository(repoFullName);

        if(bitbucketRepo.isEmpty()){
            return Optional.empty();
        }

        return bitbucketRepo.get().toStandardRepo();
    }

    @Nonnull
    @Override
    public List<PullRequest> fetchOpenPullRequests(String repoFullName) {

        var bitbucketOpenPrs= feignRemoteForBitbucketBulkActions.fetchOpenPullRequests(repoFullName);

        return bitbucketOpenPrs.stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequest::toStandardPullRequest)
                .collect(toList());
    }

    @Override
    public User fetchCurrentUser(String sourceControlAccessToken, String emailAddress) {
        return feignRemoteForBitbucketBulkActions.fetchCurrentUser(sourceControlAccessToken,emailAddress).toStandardUser();
    }
}
