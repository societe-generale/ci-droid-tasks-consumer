package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.Reference;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

import java.util.List;

interface BitBucketReferenceClient {

  @RequestLine("POST")
  @Headers("Content-Type: application/json")
  Reference createBranch(FeignRemoteForBitbucketBulkActions.InputRef inputRef) throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException;

  @RequestLine("POST")
  @Headers("Content-Type: application/json")
  PullRequest createPullRequest(PullRequestToCreate newPr) throws RemoteSourceControlAuthorizationException;

  @RequestLine("GET")
  @Headers("Content-Type: application/json")
  User getCurrentUser();

  static Feign.Builder buildBitbucketReferenceClient(String sourceControlPersonalToken) {
    return Feign.builder()
        .logger(new Slf4jLogger(BitBucketReferenceClient.class))
        .encoder(new JacksonEncoder(List.of(new JavaTimeModule())))
        .decoder(new JacksonDecoder(List.of(new JavaTimeModule())))
        .errorDecoder(new BranchCreationErrorDecoder())
        .requestInterceptor(new SourceControlApiAccessKeyInterceptor(sourceControlPersonalToken))
        .logLevel(Logger.Level.FULL);
  }
}
