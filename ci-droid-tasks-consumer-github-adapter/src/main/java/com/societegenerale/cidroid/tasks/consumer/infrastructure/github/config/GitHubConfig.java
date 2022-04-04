package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubBulkActions;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubEvents;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.GitHubEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.RemoteForGitHubBulkActionsWrapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.RemoteForGitHubEventsWrapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.SourceControlApiAccessKeyInterceptor;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.rest.GitHubSourceControlEventController;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
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
@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "GITHUB")
@EnableFeignClients(clients = { FeignRemoteForGitHubEvents.class, FeignRemoteForGitHubBulkActions.class})
@Slf4j
public class GitHubConfig {

    @Value("${source-control.url}")
    private String internalGitHubUrl;

    @Value("${source-control.url}")
    public void setInternalGitHubUrl(String gitHubApiUrl){
        log.info("Initiating GitHub API URL : {}", gitHubApiUrl);
        gitHubUrl = gitHubApiUrl;
    }

    private static String gitHubUrl;

    public static String getGitHubApiUrl() {
        return gitHubUrl;
    }

    @Bean
    public SourceControlBulkActionsPerformer remoteForGitHubBulkActionsWrapper(FeignRemoteForGitHubBulkActions feignRemoteForGitHubBulkActions) {

        return new RemoteForGitHubBulkActionsWrapper(feignRemoteForGitHubBulkActions);
    }

    @Bean
    public SourceControlEventsReactionPerformer remoteForGitHubEventsWrapper(FeignRemoteForGitHubEvents feignRemoteForGitHubEvents) {

        return new RemoteForGitHubEventsWrapper(feignRemoteForGitHubEvents);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
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
    public GitHubSourceControlEventController sourceControlEventController(
            PullRequestEventService pullRequestEventService, PushEventService pushEventService,SourceControlEventMapper sourceControlEventMapper) {

        return new GitHubSourceControlEventController(pullRequestEventService, pushEventService,sourceControlEventMapper);
    }

    @Bean
    public SourceControlEventMapper gitHubEventMapper()
    {

        return  new GitHubEventDeserializer();
    }
}
