package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.RemoteForAzureDevopsBulkActions;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import java.util.List;
import javax.annotation.Nonnull;
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
        //TODO implement a real one
        return new SourceControlEventsReactionPerformer() {
            @Nonnull
            @Override
            public List<PullRequest> fetchOpenPullRequests(String repoFullName) {
                return null;
            }

            @Override
            public PullRequest fetchPullRequestDetails(String repoFullName, int prNumber) {
                return null;
            }

            @Override
            public User fetchUser(String login) {
                return null;
            }

            @Override
            public void addCommentOnPR(String repoFullName, int prNumber, Comment comment) {

            }

            @Nonnull
            @Override
            public List<PullRequestFile> fetchPullRequestFiles(String repoFullName, int prNumber) {
                return null;
            }

            @Nonnull
            @Override
            public List<PullRequestComment> fetchPullRequestComments(String repoFullName, int prNumber) {
                return null;
            }

            @Override
            public void closePullRequest(String repoFullName, int prNumber) {

            }
        };
    }

    @Bean
    public SourceControlEventMapper gitLabEventMapper()
    {
        //TODO set an Azuredevops one, if required
        return null;
    }


}
