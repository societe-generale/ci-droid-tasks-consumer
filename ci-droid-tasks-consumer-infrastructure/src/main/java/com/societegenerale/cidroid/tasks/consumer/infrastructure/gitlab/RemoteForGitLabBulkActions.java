package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
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
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.RepositoryFile;

@Slf4j
public class RemoteForGitLabBulkActions implements SourceControlBulkActionsPerformer {

  private final Logger gitLabLogger=Logger.getLogger(RemoteForGitLabBulkActions.class.toString());

  private GitLabApi gitlabClient;

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

    try {
      var gitLabMergeRequest=gitlabClient.getMergeRequestApi().createMergeRequest(repoFullName,newPr.getBase(),newPr.getHead(),newPr.getTitle(),"",null);

      var pullRequest=new PullRequest(gitLabMergeRequest.getId());
      pullRequest.setHtmlUrl(gitLabMergeRequest.getWebUrl());

      return pullRequest;

    } catch (GitLabApiException e) {
      log.error("problem while creating a pull request",e);
    }

    return null;

  }

  @Override
  public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {

    try {
      var lastCommit=gitlabClient.getCommitsApi().getCommits(repoFullName, branchName,null,null).get(0);

      return new Reference(
          REFS_HEADS + branchName, new Reference.ObjectReference("commit", lastCommit.getId()));

    } catch (GitLabApiException e) {
      log.error("problem while fetching head",e);
    }
    return null;
  }


  @Override
  public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String oauthToken)
      throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

    try {
      var newBranch=gitlabClient.getRepositoryApi().createBranch(repoFullName,branchName,fromReferenceSha1);

      return new Reference(
          REFS_HEADS + newBranch.getName(), new Reference.ObjectReference("commit", fromReferenceSha1));


    } catch (GitLabApiException e) {
      log.error("problem while creating a branch",e);
    }

    return null;
  }

  @Override
  public Optional<Repository> fetchRepository(String repoFullName) {


    var optionalGitLabRepo=gitlabClient.getProjectApi().getOptionalProject(repoFullName);

    if(optionalGitLabRepo.isEmpty()){
      return Optional.empty();
    }

    var gitLabRepo=optionalGitLabRepo.get();

    var repository=new Repository();
    repository.setFullName(repoFullName);
    repository.setId(gitLabRepo.getId());
    repository.setDefaultBranch(gitLabRepo.getDefaultBranch());

    return Optional.of(repository);
  }

  @Nonnull
  @Override
  public List<PullRequest> fetchOpenPullRequests(String repoFullName) {

    try {
      List<MergeRequest> mergeRequests = gitlabClient.getMergeRequestApi().getMergeRequests(repoFullName);

      return mergeRequests.stream().map(RemoteForGitLabBulkActions::toPullRequest).collect(toList());
    }
    catch (GitLabApiException e) {
      log.error("could not retrieve the list of merge requests for project "+repoFullName,e);
    }

    return emptyList();
  }

  @Override
  public User fetchCurrentUser(String oAuthToken) {
    return null;
  }

  @Override
  public PullRequest fetchPullRequestDetails(String repoFullName, int prNumber) {
    return null;
  }

  private static PullRequest toPullRequest(MergeRequest mr) {

    PullRequest pr=new PullRequest(mr.getId());

    //TODO map other fields.

    return pr;
  }


}
