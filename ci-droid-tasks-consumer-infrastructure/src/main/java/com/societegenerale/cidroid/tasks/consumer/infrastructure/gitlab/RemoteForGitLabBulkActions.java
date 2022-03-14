package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource.Content;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource.UpdateStatus;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.Constants.Encoding;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.CommitAction;
import org.gitlab4j.api.models.CommitAction.Action;
import org.gitlab4j.api.models.CommitPayload;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.RepositoryFile;

@Slf4j
public class RemoteForGitLabBulkActions implements SourceControlBulkActionsPerformer {

  private static final Logger gitLabLogger=Logger.getLogger(RemoteForGitLabEventsActions.class.toString());

  private final String gitLabApiUrl;

  private final GitLabApi readOnlyGitlabClient;

  public RemoteForGitLabBulkActions(String gitLabApiUrl, String apiKeyForReadOnlyAccess) {
    this.gitLabApiUrl=gitLabApiUrl;

    readOnlyGitlabClient = new GitLabApi(gitLabApiUrl, apiKeyForReadOnlyAccess);
    readOnlyGitlabClient.enableRequestResponseLogging(gitLabLogger, Level.INFO,1024);
  }

  @Override
  public ResourceContent fetchContent(String repoFullName, String path, String branch) {

    try {
      RepositoryFile file = readOnlyGitlabClient.getRepositoryFileApi().getFile(repoFullName,path,branch);

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
  public UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
      throws RemoteSourceControlAuthorizationException {

    var user=fetchCurrentUser(sourceControlAccessToken);

    CommitAction commit=new CommitAction();
    commit.setContent(directCommit.getBase64EncodedContent());
    commit.setEncoding(Encoding.BASE64);
    commit.setFilePath(path);
    //TODO what is it's not an update but a create ?
    commit.setAction(Action.UPDATE);


    CommitPayload commitPayload=new CommitPayload();
    commitPayload.setBranch(directCommit.getBranch());
    commitPayload.setCommitMessage(directCommit.getCommitMessage());
    commitPayload.setAuthorName(user.getLogin());
    commitPayload.setAuthorEmail(user.getEmail());

    commitPayload.setActions(List.of(commit));

    try {

      var commitPerformed=getReadWriteGitLabClient(sourceControlAccessToken).getCommitsApi().createCommit(repoFullName,commitPayload);

      Commit commitOnUpdatedResource=new Commit();
      commitOnUpdatedResource.setId(commitPerformed.getId());
      commitOnUpdatedResource.setAuthor(user);

      var content= new Content();
      content.setHtmlUrl(commitPerformed.getWebUrl());

      return UpdatedResource.builder()
          .updateStatus(UpdateStatus.UPDATE_OK)
          .commit(commitOnUpdatedResource)
          .content(content)
          .build();

    } catch (GitLabApiException e) {
      throw new RemoteSourceControlAuthorizationException("problem while trying to commit content in branch "
          +directCommit.getBranch()+" on repo "+repoFullName, e);
    }
  }

  @Override
  public UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
      throws RemoteSourceControlAuthorizationException {
    return null;
  }

  @Override
  public PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlAccessToken)
      throws RemoteSourceControlAuthorizationException {

    try {
      var gitLabMergeRequest=getReadWriteGitLabClient(sourceControlAccessToken).getMergeRequestApi()
          .createMergeRequest(repoFullName,
                              newPr.getHead(),
                              newPr.getBase(),
                              newPr.getTitle(),"",null);

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
      var lastCommit=readOnlyGitlabClient.getCommitsApi().getCommits(repoFullName, branchName,null,null).get(0);

      return new Reference(
          REFS_HEADS + branchName, new Reference.ObjectReference("commit", lastCommit.getId()));

    } catch (GitLabApiException e) {
      log.error("problem while fetching head",e);
    }
    return null;
  }


  @Override
  public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlAccessToken)
      throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

    try {
      var branch=readOnlyGitlabClient.getRepositoryApi().getOptionalBranch(repoFullName,branchName);

      if(branch.isPresent()){
        throw new BranchAlreadyExistsException("branch "+branchName+" already exists on repo "+repoFullName);
      }

    } catch (GitLabApiException e) {
      throw new RemoteSourceControlAuthorizationException("problem while checking if branch "+branchName+" already exists on repo "+repoFullName,e);
    }

    try {
      var newBranch=getReadWriteGitLabClient(sourceControlAccessToken).getRepositoryApi().createBranch(repoFullName,branchName,fromReferenceSha1);

      return new Reference(
          REFS_HEADS + newBranch.getName(), new Reference.ObjectReference("commit", fromReferenceSha1));

    } catch (GitLabApiException e) {
      throw new RemoteSourceControlAuthorizationException("problem while creating a branch",e);
    }

  }

  @Override
  public Optional<Repository> fetchRepository(String repoFullName) {

    var optionalGitLabRepo=readOnlyGitlabClient.getProjectApi().getOptionalProject(repoFullName);

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

      var openMergeRequestOnThisRepoFilter=new MergeRequestFilter()
          .withProjectId(readOnlyGitlabClient.getProjectApi().getProject(repoFullName).getId())
          .withState(MergeRequestState.OPENED);

      List<MergeRequest> mergeRequests = readOnlyGitlabClient.getMergeRequestApi()
          .getMergeRequests(openMergeRequestOnThisRepoFilter);

      return mergeRequests.stream().map(RemoteForGitLabBulkActions::toPullRequest).collect(toList());
    }
    catch (GitLabApiException e) {
      log.error("could not retrieve the list of merge requests for project "+repoFullName,e);
    }

    return emptyList();
  }

  @Override
  public User fetchCurrentUser(String sourceControlAccessToken) {

    try {
      var gitLabUser=getReadWriteGitLabClient(sourceControlAccessToken).getUserApi().getCurrentUser();
      return new User(gitLabUser.getUsername(),gitLabUser.getEmail());
    } catch (GitLabApiException e) {
      log.warn("could not find GitLab user with provided apiKey",e);
    }

    return null;
  }


  private static PullRequest toPullRequest(MergeRequest mr) {

    return new PullRequest(mr.getId(),mr.getSourceBranch());
    //map other fields if required.

  }

  private GitLabApi getReadWriteGitLabClient(String apiKey){
    var rwGitLabClient = new GitLabApi(gitLabApiUrl,apiKey);

    rwGitLabClient.enableRequestResponseLogging(gitLabLogger, Level.INFO,1024);
    return rwGitLabClient;
  }


}
