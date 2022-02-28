package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;

@Slf4j
public class RemoteForGitLabEventsActions implements SourceControlEventsReactionPerformer {

  private final Logger gitLabLogger=Logger.getLogger(RemoteForGitLabEventsActions.class.toString());

  private final GitLabApi gitlabClient;

  public RemoteForGitLabEventsActions(String gitLabUrl, String privateToken) {
    this.gitlabClient = new GitLabApi(gitLabUrl, privateToken);
    this.gitlabClient.enableRequestResponseLogging(gitLabLogger, Level.INFO,1024);
  }

  @Nonnull
  @Override
  public List<PullRequest> fetchOpenPullRequests(String repoFullName) {
    return null;
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

    try {
      var gitLabUser = gitlabClient.getUserApi().getCurrentUser();

      return new User(gitLabUser.getUsername(),gitLabUser.getEmail());

    } catch (GitLabApiException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public void addCommentOnPR(String repoFullName, int prNumber, Comment comment) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Nonnull
  @Override
  public List<PullRequestFile> fetchPullRequestFiles(String repoFullName, int prNumber) {
    throw new UnsupportedOperationException("not implemented yet");
  }



  @Nonnull
  @Override
  public List<PullRequestComment> fetchPullRequestComments(String repoFullName, int prNumber) {
    throw new UnsupportedOperationException("not implemented yet");
  }


  @Override
  public void closePullRequest(String repoFullName, int prNumber) {
    throw new UnsupportedOperationException("not implemented yet");
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

  @Override
  public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {
    return null;
  }

  private static PullRequest toPullRequest(MergeRequest mr) {

    PullRequest pr=new PullRequest(mr.getId());

    //TODO map other fields.

    return pr;
  }
}
