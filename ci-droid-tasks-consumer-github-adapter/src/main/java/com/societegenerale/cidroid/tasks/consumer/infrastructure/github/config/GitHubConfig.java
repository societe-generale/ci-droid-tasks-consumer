package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubBulkActions;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubEvents;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.GitHubEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.SourceControlApiAccessKeyInterceptor;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.rest.GitHubSourceControlEventController;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "GITHUB")
@EnableFeignClients(clients = { FeignRemoteForGitHubEvents.class, FeignRemoteForGitHubBulkActions.class})
public class GitHubConfig {

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
