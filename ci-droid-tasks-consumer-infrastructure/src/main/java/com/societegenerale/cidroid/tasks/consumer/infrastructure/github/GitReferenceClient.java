package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

interface GitReferenceClient {

  @RequestLine("POST")
  @Headers("Content-Type: application/json")
  Reference createBranch(FeignRemoteForGitHubBulkActions.InputRef inputRef) throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException;

  @RequestLine("POST")
  @Headers("Content-Type: application/json")
  PullRequest createPullRequest(PullRequestToCreate newPr) throws RemoteSourceControlAuthorizationException;

  @RequestLine("GET")
  @Headers("Content-Type: application/json")
  User getCurrentUser();

  static Feign.Builder buildGitReferenceClient(String sourceControlPersonalToken) {
    return Feign.builder()
        .logger(new Slf4jLogger(GitReferenceClient.class))
        .encoder(new JacksonEncoder())
        .decoder(new JacksonDecoder())
        .errorDecoder(new BranchCreationErrorDecoder())
        .requestInterceptor(new SourceControlApiAccessKeyInterceptor(sourceControlPersonalToken))
        .logLevel(Logger.Level.FULL);
  }
}