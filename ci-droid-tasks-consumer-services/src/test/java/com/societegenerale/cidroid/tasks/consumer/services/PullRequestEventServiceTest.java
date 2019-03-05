package com.societegenerale.cidroid.tasks.consumer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestEvent;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class PullRequestEventServiceTest {

    PullRequestEventService pullRequestEventService;

    PullRequestEventHandler mockHandler = mock(PullRequestEventHandler.class);

    PullRequestEvent pullRequestEvent;

    @BeforeEach
    void setUp() throws IOException {

        pullRequestEventService = new PullRequestEventService(Arrays.asList(mockHandler));

        String pullRequestEventPayload = IOUtils
                .toString(PullRequestEventServiceTest.class.getClassLoader().getResourceAsStream("pullRequestEvent.json"), "UTF-8");

        pullRequestEvent = new ObjectMapper().readValue(pullRequestEventPayload, PullRequestEvent.class);

    }

    @Test
    void shouldProcessPullRequestEventsThatAre_Opened() {

        pullRequestEventService.onGitHubPullRequestEvent(pullRequestEvent);

        verify(mockHandler, times(1)).handle(pullRequestEvent);

    }

    @Test
    void shouldProcessPullRequestEventsThatAre_Synchronize() {

        pullRequestEvent.setAction("synchronize");

        pullRequestEventService.onGitHubPullRequestEvent(pullRequestEvent);

        verify(mockHandler, times(1)).handle(pullRequestEvent);

    }

    @Test
    void should_NOT_ProcessPullRequestEventsThatAre_Assigned() {

        pullRequestEvent.setAction("assigned");

        pullRequestEventService.onGitHubPullRequestEvent(pullRequestEvent);

        verify(mockHandler, never()).handle(pullRequestEvent);

    }

}