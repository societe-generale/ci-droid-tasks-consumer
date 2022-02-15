package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.GitLabEventDeserializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
/**
 * Needed for SourceControlEventControllerTest
 */
public class TestApplication {

    @Configuration
    class SourceControlEventControllerTestConfig {

        @Bean
        public SourceControlEventMapper gitLabEventDeserializer() {
            return new GitLabEventDeserializer();
        }

    }
}
