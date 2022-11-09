package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.BitbucketPullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.BitbucketPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;

public class BitbucketEventDeserializer implements SourceControlEventMapper {

    private final ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public PushEvent deserializePushEvent(String rawPushEvent) throws JsonProcessingException {
        BitbucketPushEvent pushEvent=objectMapper.readValue(rawPushEvent, BitbucketPushEvent.class);
        pushEvent.setRawEvent(rawPushEvent);

        return pushEvent;
    }

    @Override
    public PullRequestEvent deserializePullRequestEvent(String rawPullRequestEvent) throws JsonProcessingException {

        objectMapper.registerModule(new JavaTimeModule());
        BitbucketPullRequestEvent pullRequestEvent=objectMapper.readValue(rawPullRequestEvent, BitbucketPullRequestEvent.class);
        pullRequestEvent.setRawEvent(rawPullRequestEvent);

        return pullRequestEvent;
    }
}
