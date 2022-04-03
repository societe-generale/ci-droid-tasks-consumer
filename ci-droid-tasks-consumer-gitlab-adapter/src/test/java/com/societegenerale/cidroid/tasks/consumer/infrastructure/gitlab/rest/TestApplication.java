package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.rest;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.GitLabEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class TestApplication {

    @Configuration
    static class SourceControlEventControllerTestConfig {

        @Bean
        public SourceControlEventMapper gitLabEventDeserializer() {
            return new GitLabEventDeserializer();
        }

    }
}
