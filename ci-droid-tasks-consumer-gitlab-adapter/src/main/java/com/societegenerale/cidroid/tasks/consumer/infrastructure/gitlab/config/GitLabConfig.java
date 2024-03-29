package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.GitLabEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.RemoteForGitLabBulkActions;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.RemoteForGitLabEventsActions;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.rest.GitLabSourceControlEventController;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "GITLAB")
public class GitLabConfig {

    @Bean
    public GitLabSourceControlEventController gitLabSourceControlEventController(
            PullRequestEventService pullRequestEventService,
            PushEventService pushEventService,
            SourceControlEventMapper gitLabEventMapper)
    {
        return new GitLabSourceControlEventController(pullRequestEventService,pushEventService,gitLabEventMapper);
    }

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
        @Value("${source-control.apiToken:#{null}}") String apiKeyForReadOnlyAccess)
    {
        return new RemoteForGitLabBulkActions(gitLabApiUrl,apiKeyForReadOnlyAccess);
    }

}
