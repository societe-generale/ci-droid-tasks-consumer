package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RemoteGitHubConfig {

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  RequestInterceptor oauthTokenSetterInterceptor(@Value("${source-control.oauthToken:#{null}}") String oauthToken) {
    return new OAuthInterceptor(oauthToken);
  }

  /**
   * adding an ApacheHttpClient to enable PATCH requests with Feign
   */
  @Bean
  Client apacheHttpClient() {
    return new ApacheHttpClient();
  }

}
