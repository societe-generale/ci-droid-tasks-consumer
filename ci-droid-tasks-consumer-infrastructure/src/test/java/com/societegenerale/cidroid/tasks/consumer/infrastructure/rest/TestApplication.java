package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
/**
 * Needed for SourceControlEventControllerTest
 */
public class TestApplication {

    @Configuration
    static class SourceControlEventControllerTestConfig {

        @Bean
        public SourceControlEventMapper gitLabEventDeserializer() {
            return null;
        }

    }
}
