package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import feign.Headers;
import feign.RequestLine;

interface ContentClient {

  @RequestLine("PUT")
  @Headers("Content-Type: application/json")
  UpdatedResource updateContent(DirectCommit directCommit) throws RemoteSourceControlAuthorizationException;

  @RequestLine("DELETE")
  @Headers("Content-Type: application/json")
  UpdatedResource deleteResource(DirectCommit directCommit) throws RemoteSourceControlAuthorizationException;
}
