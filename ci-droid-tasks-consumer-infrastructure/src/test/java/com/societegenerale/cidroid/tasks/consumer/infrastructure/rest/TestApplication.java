package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.GitLabEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
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
