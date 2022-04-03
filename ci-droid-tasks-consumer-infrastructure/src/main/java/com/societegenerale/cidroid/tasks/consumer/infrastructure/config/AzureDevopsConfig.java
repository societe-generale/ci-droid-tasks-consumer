package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.RemoteForAzureDevopsBulkActions;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "AZURE_DEVOPS")
public class AzureDevopsConfig {

    @Bean
    public SourceControlBulkActionsPerformer azureDevopsClientForBulkActions(@Value("${source-control.apiToken}") String apiKeyForReadOnlyAccess,
        @Value("${source-control.organization-name}") String orgNameToSplit)
    {
        return new RemoteForAzureDevopsBulkActions(null,apiKeyForReadOnlyAccess,orgNameToSplit);
    }

    @Bean
    public SourceControlEventsReactionPerformer azureDevopsClientForEventsReaction()
    {
        return null;
    }

    @Bean
    public SourceControlEventMapper gitLabEventMapper()
    {
        //TODO set an Azuredevops one
        return null;
    }


}
