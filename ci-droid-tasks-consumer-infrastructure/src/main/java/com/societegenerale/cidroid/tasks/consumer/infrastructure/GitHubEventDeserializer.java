package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;

public class GitHubEventDeserializer implements SourceControlEventMapper {

    private ObjectMapper objectMapper=new ObjectMapper();

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
