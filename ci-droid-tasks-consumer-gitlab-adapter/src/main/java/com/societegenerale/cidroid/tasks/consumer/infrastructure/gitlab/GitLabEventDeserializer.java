package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.gitlab.GitLabMergeRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.gitlab.GitLabPushEvent;

public class GitLabEventDeserializer implements SourceControlEventMapper {

    private final ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public PushEvent deserializePushEvent(String rawPushEvent) throws JsonProcessingException {

        GitLabPushEvent pushEvent=objectMapper.readValue(rawPushEvent, GitLabPushEvent.class);
        pushEvent.setRawEvent(rawPushEvent);

        return pushEvent;
    }

    @Override
    public PullRequestEvent deserializePullRequestEvent(String rawPullRequestEvent) throws JsonProcessingException {

        GitLabMergeRequestEvent pullRequestEvent=objectMapper.readValue(rawPullRequestEvent, GitLabMergeRequestEvent.class);
        pullRequestEvent.setRawEvent(rawPullRequestEvent);

        return pullRequestEvent;
    }
}
