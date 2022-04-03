package com.societegenerale.cidroid.tasks.consumer.services;

import java.util.Arrays;

import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class PullRequestEventServiceTest {

    PullRequestEventHandler mockHandler = mock(PullRequestEventHandler.class);

    private final Repository repo=Repository.builder().url("someUrl").name("someName").build();


    TestPullRequestEvent.TestPullRequestEventBuilder pullRequestEventBuilder=TestPullRequestEvent.builder()
            .action("synchronize")
            .repository(repo);

    PullRequestEventService pullRequestEventService= new PullRequestEventService(Arrays.asList(mockHandler));

    @Test
    public void shouldProcessPullRequestEventsThatAre_Opened() {

        var pullRequestEvent=pullRequestEventBuilder
                .build();

        pullRequestEventService.onPullRequestEvent(pullRequestEvent);

        verify(mockHandler, times(1)).handle(pullRequestEvent);

    }

    @Test
    public void shouldProcessPullRequestEventsThatAre_Synchronize() {

        var pullRequestEvent=pullRequestEventBuilder.build();

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
