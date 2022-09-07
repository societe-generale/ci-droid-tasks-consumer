package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
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
