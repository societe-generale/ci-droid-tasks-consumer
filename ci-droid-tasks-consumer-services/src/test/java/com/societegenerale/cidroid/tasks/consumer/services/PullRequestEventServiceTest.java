package com.societegenerale.cidroid.tasks.consumer.services;

import java.io.IOException;
import java.util.Arrays;

import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class PullRequestEventServiceTest {

    PullRequestEventService pullRequestEventService;

    PullRequestEventHandler mockHandler = mock(PullRequestEventHandler.class);

    TestPullRequestEvent.TestPullRequestEventBuilder pullRequestEventBuilder=TestPullRequestEvent.builder();

    @BeforeEach
    public void setUp() throws IOException {

        pullRequestEventService = new PullRequestEventService(Arrays.asList(mockHandler));

    }

    @Test
    public void shouldProcessPullRequestEventsThatAre_Opened() {

        var pullRequestEvent=pullRequestEventBuilder.build();

        pullRequestEventService.onPullRequestEvent(pullRequestEvent);

        verify(mockHandler, times(1)).handle(pullRequestEvent);

    }

    @Test
    public void shouldProcessPullRequestEventsThatAre_Synchronize() {

        var pullRequestEvent=pullRequestEventBuilder
                .action("synchronize").build();

        pullRequestEventService.onPullRequestEvent(pullRequestEvent);

        verify(mockHandler, times(1)).handle(pullRequestEvent);

    }

    @Test
    public void should_NOT_ProcessPullRequestEventsThatAre_Assigned() {

        var pullRequestEvent=pullRequestEventBuilder
                .action("assigned").build();

        pullRequestEventService.onPullRequestEvent(pullRequestEvent);

        verify(mockHandler, never()).handle(pullRequestEvent);

    }

}
