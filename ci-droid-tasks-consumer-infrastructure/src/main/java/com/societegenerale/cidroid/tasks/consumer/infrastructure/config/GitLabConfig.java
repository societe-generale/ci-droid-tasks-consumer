package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.GitLabEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.RemoteForGitLabBulkActions;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.RemoteForGitLabEventsActions;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "GITLAB")
public class GitLabConfig {

    @Bean
    public SourceControlEventMapper gitLabEventMapper()
    {

        return new GitLabEventDeserializer();
    }



    @Bean
    public SourceControlEventsReactionPerformer gitLabClient(
        @Value("${source-control.url}") String url,
        @Value("${source-control.apiToken:#{null}}") String apiToken)
    {

        return  new RemoteForGitLabEventsActions(url,apiToken);
    }

    @Bean
    public SourceControlBulkActionsPerformer gitLabClientForBulkActions(
        @Value("${source-control.url}") String gitLabApiUrl,
        @Value("${source-control.oauthToken:#{null}}") String apiKeyForReadOnlyAccess)
    {
        return new RemoteForGitLabBulkActions(gitLabApiUrl,apiKeyForReadOnlyAccess);
    }

}
