package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab;

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

@Slf4j
//TODO implement it !
public class GitLabClient implements RemoteSourceControl {

  public GitLabClient() {
    log.info("instantiating a GitLab client");

  }

  @Nonnull
  @Override
  public List<PullRequest> fetchOpenPullRequests(String repoFullName) {
    return null;
  }

  @Override
  public PullRequest fetchPullRequestDetails(String repoFullName, int prNumber) {
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
  public PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String oauthToken) throws RemoteSourceControlAuthorizationException {
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
}
