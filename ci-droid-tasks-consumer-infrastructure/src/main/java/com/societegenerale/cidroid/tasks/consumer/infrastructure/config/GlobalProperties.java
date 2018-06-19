package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GlobalProperties {

    private static String gitHubUrl = null;

    public static String getGitHubUrl() {
        return gitHubUrl;
    }

    @Value("${gitHub.url}")
    public void setGithubInstanceUrl(String gitHubUrlFromProperties) {

        gitHubUrl = gitHubUrlFromProperties;

        log.info("Initiating GitHub instance URL : {}", gitHubUrl);
    }

}
