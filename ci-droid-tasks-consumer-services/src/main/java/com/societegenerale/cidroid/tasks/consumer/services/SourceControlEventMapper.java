package com.societegenerale.cidroid.tasks.consumer.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;

public interface SourceControlEventMapper {

    PushEvent deserializePushEvent(String rawPushEvent) throws JsonProcessingException;

    PullRequestEvent deserializePullRequestEvent(String rawPullrequestEvent) throws JsonProcessingException;
}
