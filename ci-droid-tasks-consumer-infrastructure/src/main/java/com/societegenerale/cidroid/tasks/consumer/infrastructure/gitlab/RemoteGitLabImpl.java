package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.RepositoryFile;

@Slf4j
public class RemoteGitLabImpl implements RemoteSourceControl {

    private final GitLabApi gitlabClient;

    public RemoteGitLabImpl(String gitLabUrl, String privateToken) {
        this.gitlabClient = new GitLabApi(gitLabUrl, privateToken);
    }

    @Nonnull
    @Override
    public List<PullRequest> fetchOpenPullRequests(String repoFullName) {

        try {
            List<MergeRequest> mergeRequests = gitlabClient.getMergeRequestApi().getMergeRequests(repoFullName);

            return mergeRequests.stream().map(mr -> toPullRequest(mr)).collect(toList());
        }
        catch (GitLabApiException e) {
            log.error("could not retrieve the list of merge requests for project "+repoFullName,e);
        }

        return emptyList();
    }

    @Override
    public PullRequest fetchPullRequestDetails(String repoFullName, int prNumber) {

        try {
            return toPullRequest(gitlabClient.getMergeRequestApi().getMergeRequest(repoFullName,prNumber));
        } catch (GitLabApiException e) {
            log.error("could not retrieve the details of merge request "+prNumber+" for project "+repoFullName,e);
        }

        return null;
    }

    @Override
    public User fetchUser(String login) {
        return null;
    }

    @Override
    public User fetchCurrentUser(String oAuthToken) {
        return null;
    }

    @Override
    public void addCommentOnPR(String repoFullName, int prNumber, Comment comment) {

    }

    @Nonnull
    @Override
    public List<PullRequestFile> fetchPullRequestFiles(String repoFullName, int prNumber) {
        return null;
    }

    @Nonnull
    @Override
    public List<PullRequestComment> fetchPullRequestComments(String repoFullName, int prNumber) {
        return null;
    }

    @Override
    public ResourceContent fetchContent(String repoFullName, String path, String branch) {

        try {
            RepositoryFile file = gitlabClient.getRepositoryFileApi().getFile(path, repoFullName, branch);

            var fileContent= new ResourceContent();
            fileContent.setBase64EncodedContent(file.getContent());
            fileContent.setSha(file.getCommitId());
            return fileContent;


        } catch (GitLabApiException e) {
            log.error("could not retrieve the content of file "+path+" for project "+repoFullName+" on branch "+branch,e);
        }

        return null;
    }

    @Override
    public UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String oauthToken)
            throws RemoteSourceControlAuthorizationException {
        return null;
    }

    @Override
    public UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String oauthToken)
            throws RemoteSourceControlAuthorizationException {
        return null;
    }

    @Override
    public PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String oauthToken)
            throws RemoteSourceControlAuthorizationException {
        return null;
    }

    @Override
    public void closePullRequest(String repoFullName, int prNumber) {

    }

    @Override
    public Optional<Repository> fetchRepository(String repoFullName) {
        return Optional.empty();
    }

    @Override
    public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {
        return null;
    }

    @Override
    public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String oauthToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {
        return null;
    }


    private static PullRequest toPullRequest(MergeRequest mr) {

        PullRequest pr=new PullRequest(mr.getId());

        //TODO map other fields.

        return pr;
    }
}
