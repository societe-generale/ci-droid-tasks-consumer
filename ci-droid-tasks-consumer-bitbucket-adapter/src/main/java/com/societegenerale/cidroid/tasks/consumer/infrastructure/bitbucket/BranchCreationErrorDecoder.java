package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class BranchCreationErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {

    if (response.status() == 422) {
      return new BranchAlreadyExistsException("Branch or PR seems to already exist : " + response.reason());
    }

    if (response.status() == 401) {
      return new RemoteSourceControlAuthorizationException("Issue with credentials provided : " + response.reason());
    }

    return FeignException.errorStatus(methodKey, response);
  }
}
