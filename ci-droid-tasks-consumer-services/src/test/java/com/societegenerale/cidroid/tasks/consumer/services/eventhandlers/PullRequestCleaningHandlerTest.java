package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.function.Supplier;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.TestPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.OLD_PR_CLOSED;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PullRequestCleaningHandlerTest {

    private static final int PR_AGE_LIMIT_IN_DAYS = 180;
    private static final int PULL_REQUEST_NUMBER = 7;

    private PullRequestCleaningHandler pullRequestCleaningHandler;
    private SourceControlEventsReactionPerformer remoteSourceControl;
    private PushEvent pushEvent=TestPushEvent.builder()
            .repository(Repository.builder().fullName("aRepoName").build())
            .build();

    private final TestAppender testAppender = new TestAppender();

    private final Supplier<LocalDateTime> dateProvider = () -> LocalDateTime.of(2018, 12, 23, 16, 0, 0);

    @BeforeEach
    public void setUp() throws IOException {
        remoteSourceControl = mock(SourceControlEventsReactionPerformer.class);

        pullRequestCleaningHandler = new PullRequestCleaningHandler(
                remoteSourceControl,
                dateProvider,
                PR_AGE_LIMIT_IN_DAYS
        );

        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger log = logCtx.getLogger("Main");
        log.addAppender(testAppender);
    }

    @Test
    void shouldClosePRWhichOutlivedTheLimit() {
        LocalDateTime oldDate = dateProvider.get().minusDays(PR_AGE_LIMIT_IN_DAYS + 10);
        PullRequest oldPullRequest = new PullRequest(PULL_REQUEST_NUMBER);
        oldPullRequest.setCreationDate(oldDate);

        pullRequestCleaningHandler.handle(pushEvent, Collections.singletonList(oldPullRequest));

        String expectedRepositoryName = pushEvent.getRepository().getFullName();
        verify(remoteSourceControl, times(1))
                .closePullRequest(expectedRepositoryName, PULL_REQUEST_NUMBER);

        assertThat(testAppender.events.stream()
                .filter(logEvent -> logEvent.getMDCPropertyMap().getOrDefault("metricName", "NOT_FOUND").equals(OLD_PR_CLOSED)).findAny())
                .isPresent();
    }

    @Test
    void shouldNotClosePRWhichAgeIsBelowTheLimit() {

        LocalDateTime recentDate = dateProvider.get().minusDays(1);
        PullRequest recentPullRequest = new PullRequest(PULL_REQUEST_NUMBER);
        recentPullRequest.setCreationDate(recentDate);

        pullRequestCleaningHandler.handle(pushEvent, Collections.singletonList(recentPullRequest));

        verifyNoInteractions(remoteSourceControl);

        assertThat(testAppender.events.stream()
                .filter(logEvent -> logEvent.getMDCPropertyMap().getOrDefault("metricName", "NOT_FOUND").equals(OLD_PR_CLOSED))
                .collect(toList()))
                .isEmpty();

    }

}
