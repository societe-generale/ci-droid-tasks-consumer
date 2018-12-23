package com.societegenerale.cidroid.tasks.consumer.services.actionHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.model.DateProvider;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PushEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static org.mockito.Mockito.*;

public class PullRequestCleaningHandlerTest {

    private static final int PR_AGE_LIMIT_IN_DAYS = 180;

    private PullRequestCleaningHandler pullRequestCleaningHandler;
    private RemoteGitHub remoteGitHub;
    private DateProvider dateProvider;
    private PushEvent pushEvent;

    @Before
    public void setUp() throws IOException {
        remoteGitHub = mock(RemoteGitHub.class);

        dateProvider = () -> LocalDateTime.of(2018, 12, 23, 16, 0, 0);

        pullRequestCleaningHandler = new PullRequestCleaningHandler(
                remoteGitHub,
                dateProvider,
                PR_AGE_LIMIT_IN_DAYS
        );

        String pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/pushEvent.json"));
        pushEvent = new ObjectMapper().readValue(pushEventPayload, PushEvent.class);
    }

    @Test
    public void shouldClosePRWhichOutlivedTheLimit() {
        int pullRequestNumber = 7;
        LocalDateTime oldDate = dateProvider.now().minusDays(PR_AGE_LIMIT_IN_DAYS + 10);
        PullRequest oldPullRequest = new PullRequest(pullRequestNumber);
        oldPullRequest.setCreationDate(oldDate);

        pullRequestCleaningHandler.handle(pushEvent, Collections.singletonList(oldPullRequest));

        String expectedRepositoryName = pushEvent.getRepository().getFullName();
        verify(remoteGitHub, times(1))
                .closePullRequest(expectedRepositoryName, pullRequestNumber);
    }

    @Test
    public void shouldNotClosePRWhichAgeIsBelowTheLimit() {
        int pullRequestNumber = 5;
        LocalDateTime recentDate = dateProvider.now().minusDays(1);
        PullRequest recentPullRequest = new PullRequest(pullRequestNumber);
        recentPullRequest.setCreationDate(recentDate);

        pullRequestCleaningHandler.handle(pushEvent, Collections.singletonList(recentPullRequest));

        verifyZeroInteractions(remoteGitHub);
    }

}