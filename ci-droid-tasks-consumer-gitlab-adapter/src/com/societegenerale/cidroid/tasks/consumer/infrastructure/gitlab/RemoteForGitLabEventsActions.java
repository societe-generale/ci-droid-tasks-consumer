package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;

@Slf4j
public class RemoteForGitLabEventsActions implements SourceControlEventsReactionPerformer {

  private final static Logger gitLabLogger=Logger.getLogger(RemoteForGitLabEventsActions.class.toString());

  private final GitLabApi gitlabClient;

  public RemoteForGitLabEventsActions(String gitLabUrl, String privateToken) {
    this.gitlabClient = new GitLabApi(gitLabUrl, privateToken);
    this.gitlabClient.enableRequestResponseLogging(gitLabLogger, Level.INFO,1024);
  }

  @Nonnull
  @Override
  public List<PullRequest> fetchOpenPullRequests(String repoFullName) {
    throw new UnsupportedOperationException("not implemented yet");
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

  private static PullRequest toPullRequest(MergeRequest mr) {

    return new PullRequest(mr.getId(),mr.getSourceBranch());
    //map other fields if required.
  }

  @Override
  public User fetchUser(String login) {
    throw new UnsupportedOperationException("not implemented yet");
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

}
