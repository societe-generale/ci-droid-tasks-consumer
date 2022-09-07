package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class SourceControlApiAccessKeyInterceptor implements RequestInterceptor {
  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final String sourceControlAccessToken;

  public SourceControlApiAccessKeyInterceptor(String sourceControlAccessToken) {
    this.sourceControlAccessToken = sourceControlAccessToken;
  }

  @Override
  public void apply(RequestTemplate requestTemplate) {

    requestTemplate.header(AUTHORIZATION_HEADER, "token " + sourceControlAccessToken);
  }
}
