package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.GitLabEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
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
    public SourceControlBulkActionsPerformer gitLabClientForBulkActions()
    {
        //TODO implement it !
        return null;
    }

    @Bean
    public SourceControlEventsReactionPerformer gitLabClientForSourceControlEventsReaction()
    {
        //TODO implement it !
        return null;
    }

}
