package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubBulkActions;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubEvents;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.GitHubEventDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "GITHUB")
@EnableFeignClients(clients = { FeignRemoteForGitHubEvents.class, FeignRemoteForGitHubBulkActions.class})
public class GitHubConfig {


    @Bean
    public SourceControlEventMapper gitHubEventMapper()
    {

        return  new GitHubEventDeserializer();
    }
}
