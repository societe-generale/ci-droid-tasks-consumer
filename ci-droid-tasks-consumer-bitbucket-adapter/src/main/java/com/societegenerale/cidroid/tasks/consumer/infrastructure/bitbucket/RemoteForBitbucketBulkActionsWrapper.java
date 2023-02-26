package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.Blame;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.Project;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.*;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class RemoteForBitbucketBulkActionsWrapper implements SourceControlBulkActionsPerformer {

    private final FeignRemoteForBitbucketBulkActions feignRemoteForBitbucketBulkActions;

    /**
     * "umbrella" project, under which the repositories are
     */
    private final Project project;

    public RemoteForBitbucketBulkActionsWrapper(FeignRemoteForBitbucketBulkActions feignRemoteForBitbucketBulkActions, String projectKey) {
        this.feignRemoteForBitbucketBulkActions = feignRemoteForBitbucketBulkActions;
        this.project = new Project(projectKey);
    }

    @Override
    public ResourceContent fetchContent(String repoFullName, String path, String branch) {

        var bitbucketResourceContent=feignRemoteForBitbucketBulkActions.fetchContent(repoFullName,path,branch);
        var findFirstCommitHash = feignRemoteForBitbucketBulkActions.fetchCommits(repoFullName, branch).getValues().stream()
                .findFirst()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.Commit::getId)
                .orElse(null);

        return ResourceContent.builder().base64EncodedContent(Base64.getEncoder()
                        .encodeToString(bitbucketResourceContent.getBytes(StandardCharsets.UTF_8)))
                        .sha(findFirstCommitHash)
                        .build();
    }

    @Override
    public UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var bitbucketDirectCommit=com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.DirectCommit.from(directCommit);

        var bitbucketUpdatedResource=feignRemoteForBitbucketBulkActions.updateContent(repoFullName,path,bitbucketDirectCommit,sourceControlAccessToken);

        Commit postPushCommit = Commit.builder().id(bitbucketUpdatedResource.getId()).author(bitbucketUpdatedResource.getAuthor().toStandardUser()).build();

        return UpdatedResource.builder().commit(postPushCommit).content(new UpdatedResource.Content()).updateStatus(UpdatedResource.UpdateStatus.UPDATE_OK).build();
    }

    @Override
    public UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var bitbucketDirectCommit=com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.DirectCommit.from(directCommit);

        var bitbucketDeletedResource=feignRemoteForBitbucketBulkActions.deleteContent(repoFullName,path,bitbucketDirectCommit,sourceControlAccessToken);
        Commit postPushCommit = Commit.builder().id(bitbucketDeletedResource.getId()).author(bitbucketDeletedResource.getAuthor().toStandardUser()).build();

        return UpdatedResource.builder().commit(postPushCommit).content(new UpdatedResource.Content()).updateStatus(UpdatedResource.UpdateStatus.UPDATE_OK).build();
    }

    @Override
    public PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlAccessToken)
            throws RemoteSourceControlAuthorizationException {

        var bitbucketPrToCreate= com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequestToCreate.from(newPr, repoFullName, project);

        var bitbucketPr=feignRemoteForBitbucketBulkActions.createPullRequest(repoFullName, bitbucketPrToCreate, sourceControlAccessToken);

        return bitbucketPr.toStandardPullRequest();
    }

    @Override
    public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {

        var bitbucketReference=feignRemoteForBitbucketBulkActions.fetchHeadReferenceFrom(repoFullName, branchName);

        return bitbucketReference.toStandardReference("/" + branchName);
    }

    @Override
    public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlAccessToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        var bitbucketReference=feignRemoteForBitbucketBulkActions.createBranch(repoFullName, branchName,fromReferenceSha1,sourceControlAccessToken);

        return bitbucketReference.toStandardReference("/" + branchName);
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

        var bitbucketOpenPrs = feignRemoteForBitbucketBulkActions.fetchOpenPullRequests(repoFullName);

        return bitbucketOpenPrs.getValues().stream()
                .map(com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequest::toStandardPullRequest)
                .collect(toList());
    }

    public User fetchCurrentUser(String sourceControlAccessToken, String emailAddress, String login) {
        return feignRemoteForBitbucketBulkActions.fetchCurrentUser(sourceControlAccessToken, login).toStandardUser();
    }
}
