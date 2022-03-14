package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "source-control")
public class SourceControlConfig {
    private SourceControlType type;
    private String url;
    private String apiToken;
    private String login;
    private String password;
}