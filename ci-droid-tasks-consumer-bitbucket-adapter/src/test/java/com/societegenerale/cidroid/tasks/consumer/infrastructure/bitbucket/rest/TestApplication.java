package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.rest;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.BitbucketEventDeserializer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class TestApplication {

    @Configuration
    static class SourceControlEventControllerTestConfig {

        @Bean
        @Primary
        public SourceControlEventMapper bitBucketEventDeserializer() {
            return new BitbucketEventDeserializer();
        }

    }
}
