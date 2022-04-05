package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.GitHubPullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.GitHubPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;

public class GitHubEventDeserializer implements SourceControlEventMapper {

    private final ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public PushEvent deserializePushEvent(String rawPushEvent) throws JsonProcessingException {

        GitHubPushEvent pushEvent=objectMapper.readValue(rawPushEvent, GitHubPushEvent.class);
        pushEvent.setRawEvent(rawPushEvent);

        return pushEvent;
    }

    @Override
    public PullRequestEvent deserializePullRequestEvent(String rawPullRequestEvent) throws JsonProcessingException {

        GitHubPullRequestEvent pullRequestEvent=objectMapper.readValue(rawPullRequestEvent, GitHubPullRequestEvent.class);
        pullRequestEvent.setRawEvent(rawPullRequestEvent);

        return pullRequestEvent;
    }
}
