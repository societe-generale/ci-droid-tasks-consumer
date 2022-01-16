package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemoteGitHubConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    RequestInterceptor oauthTokenSetterInterceptor(@Value("${gitHub.oauthToken:#{null}}") String oauthToken) {
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

