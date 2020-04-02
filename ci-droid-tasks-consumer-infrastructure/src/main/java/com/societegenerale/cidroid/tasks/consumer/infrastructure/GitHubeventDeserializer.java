package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;

public class GitHubeventDeserializer implements SourceControlEventMapper {

    private ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public PushEvent deserializePushEvent(String rawPushEvent) throws JsonProcessingException {

        return objectMapper.readValue(rawPushEvent, GitHubPushEvent.class);
    }

    @Override
    public PullRequestEvent deserializePullRequestEvent(String rawPullrequestEvent) throws JsonProcessingException {
        return objectMapper.readValue(rawPullrequestEvent, GitHubPullRequestEvent.class);
    }
}
