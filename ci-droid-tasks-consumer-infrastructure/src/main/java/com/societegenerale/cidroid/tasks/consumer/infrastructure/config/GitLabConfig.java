package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.GitLabEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("gitLab")
public class GitLabConfig {

    @Bean
    public SourceControlEventMapper gitLabEventMapper()
    {

        return  new GitLabEventDeserializer();
    }


}
