package com.societegenerale.cidroid.tasks.consumer.autoconfigure;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.CiDroidBehavior;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CiDroidBehaviorConfig {

    @Bean
    @ConfigurationProperties(prefix = "cidroid-behavior")
    public  CiDroidBehavior  ciDroidBehavior() {
        return new  CiDroidBehavior();
    }

}
