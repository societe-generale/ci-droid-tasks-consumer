package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.GitLabEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.RemoteGitLabImpl;
import org.springframework.beans.factory.annotation.Value;
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
    public RemoteSourceControl gitLabClient(
        @Value("${gitHub.api.url}") String url,
        @Value("${gitHub.oauthToken:#{null}}") String oauthToken)
    {

        return  new RemoteGitLabImpl(url,oauthToken);
    }


}
