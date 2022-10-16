package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.*;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.rest.BitBucketSourceControlEventController;
import com.societegenerale.cidroid.tasks.consumer.services.*;
import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "BITBUCKET")
@EnableFeignClients(clients = { FeignRemoteForBitbucketEvents.class, FeignRemoteForBitbucketBulkActions.class})
@Slf4j
public class BitbucketConfig {

    @Value("${source-control.url}")
    private String internalBitbucketUrl;

    @Value("${source-control.url}")
    public void setInternalBitbucketUrl(String bitbucketApiUrl){
        log.info("Initiating Bitbucket API URL : {}", bitbucketApiUrl);
        bitbucketUrl = bitbucketApiUrl;
    }

    private static String bitbucketUrl;

    public static String getBitbucket() {
        return bitbucketUrl;
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public SourceControlBulkActionsPerformer remoteForBitbucketBulkActionsWrapper(
            FeignRemoteForBitbucketBulkActions feignRemoteForBitbucketBulkActions,
            @Value("${source-control.project-key}") String projectKey, @Value("${source-control.login}") String userSlug) {

        return new RemoteForBitbucketBulkActionsWrapper(feignRemoteForBitbucketBulkActions, projectKey, userSlug);
    }

    @Bean
    public SourceControlEventsReactionPerformer remoteForBitbucketEventsWrapper(FeignRemoteForBitbucketEvents feignRemoteForBitbucketEvents) {

        return new RemoteForBitbucketEventsWrapper(feignRemoteForBitbucketEvents);
    }

    @Bean
    RequestInterceptor sourceControlApiAccessKeyInterceptor(@Value("${source-control.apiToken:#{null}}") String apiToken) {
        return new SourceControlApiAccessKeyInterceptor(apiToken);
    }

    /**
     * adding an ApacheHttpClient to enable PATCH requests with Feign
     */
    @Bean
    Client apacheHttpClient() {
        return new ApacheHttpClient();
    }

    @Bean
    @ConditionalOnProperty(name = "synchronous-mode", havingValue = "true")
    public BitBucketSourceControlEventController sourceControlEventController(
            PullRequestEventService pullRequestEventService, PushEventService pushEventService,SourceControlEventMapper sourceControlEventMapper) {

        return new BitBucketSourceControlEventController(pullRequestEventService, pushEventService, sourceControlEventMapper);
    }

    @Bean
    public SourceControlEventMapper bitbucketEventMapper() {

        return  new BitbucketEventDeserializer();
    }
}
