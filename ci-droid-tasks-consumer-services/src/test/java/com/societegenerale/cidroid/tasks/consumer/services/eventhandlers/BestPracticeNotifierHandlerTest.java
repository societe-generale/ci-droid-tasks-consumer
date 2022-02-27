package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.societegenerale.cidroid.tasks.consumer.services.ResourceFetcher;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

class BestPracticeNotifierHandlerTest {

    private static final String MATCHING_FILENAME = "commons/src/main/resources/db/changelog/changes/004-create-risk-table.yml";

    private final String REPO_FULL_NAME = "someOrg/someRepo";

    private final ResourceFetcher mockResourceFetcher = mock(ResourceFetcher.class);

    private final Notifier mockNotifier = mock(Notifier.class);

    private final SourceControlEventsReactionPerformer mockRemoteSourceControl = mock(SourceControlEventsReactionPerformer.class);

    private final Map<String, String> patternToContentMapping = new HashMap<>();

    private final BestPracticeNotifierHandler handler = new BestPracticeNotifierHandler(
            patternToContentMapping, singletonList(mockNotifier), mockRemoteSourceControl, mockResourceFetcher);

    private final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

    private final Repository repository = new Repository();

    private final PullRequestFile matchingPullRequestFile = new PullRequestFile();

    private PullRequestEvent pullRequestEvent;

    @BeforeEach
    public void setUp() {

        matchingPullRequestFile.setFilename(MATCHING_FILENAME);

        when(mockResourceFetcher.fetch("http://someUrl/liquibaseBestPractices.md")).thenReturn(Optional.of("careful with Liquibase changes !"));

        patternToContentMapping.put("**/db/changelog/**/*.yml", "http://someUrl/liquibaseBestPractices.md");

        repository.setFullName(REPO_FULL_NAME);
        pullRequestEvent = new GitHubPullRequestEvent("created", 123, repository);

    }

    @Test
    void shouldNotifyWithConfiguredContentIfMatching() {

        returnMatchingPullRequestFileWhenFetchPullRequestFiles();

        handler.handle(pullRequestEvent);

        verify(mockNotifier, times(1)).notify(any(User.class), messageCaptor.capture(), any(Map.class));
        assertThat(messageCaptor.getValue().getContent()).contains("careful with Liquibase changes !");

    }

    @Test
    void shouldNotNotifyWhenNoMatch() {

        matchingPullRequestFile.setFilename("commons/src/main/resources/db/chaelog/changes/004-create-risk-table.yml");

        handler.handle(pullRequestEvent);

        verify(mockNotifier, never()).notify(any(User.class), messageCaptor.capture(), any(Map.class));
    }

    @Test
    void shouldNotifyOnlyOnceWhenSeveralMatches() {

        PullRequestFile anotherMatchingPullRequestFile = new PullRequestFile();
        anotherMatchingPullRequestFile.setFilename("myModule/src/main/java/org/myPackage/CoucouDto.java");

        when(mockRemoteSourceControl.fetchPullRequestFiles(REPO_FULL_NAME, 123))
                .thenReturn(asList(matchingPullRequestFile, anotherMatchingPullRequestFile));
        when(mockResourceFetcher.fetch("http://someUrl/noDtoBestPractice.md")).thenReturn(Optional.of("Don't name java object with DTO suffix"));

        patternToContentMapping.put("**/*Dto.java", "http://someUrl/noDtoBestPractice.md");

        handler.handle(pullRequestEvent);

        verify(mockNotifier, times(1)).notify(any(User.class), messageCaptor.capture(), any(Map.class));
        assertThat(messageCaptor.getValue().getContent()).contains("careful with Liquibase changes !");
        assertThat(messageCaptor.getValue().getContent()).contains("Don't name java object with DTO suffix");

    }

    @Test
    void shouldLogMonitoringEventWhenCantFetchResource() {

        returnMatchingPullRequestFileWhenFetchPullRequestFiles();

        Appender appender = mock(Appender.class);

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(Logger.ROOT_LOGGER_NAME);
        when(appender.getName()).thenReturn("MOCK");
        when(appender.isStarted()).thenReturn(true);
        logger.addAppender(appender);

        String urlWithNoResource = "http://someUrl/liquibaseBestPractices.md";

        when(mockResourceFetcher.fetch(urlWithNoResource)).thenReturn(Optional.empty());

        handler.handle(pullRequestEvent);

        verify(mockNotifier, times(1)).notify(any(User.class), any(Message.class), any(Map.class));

        ArgumentCaptor<ILoggingEvent> logEventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);

        verify(appender, atLeastOnce()).doAppend(logEventCaptor.capture());

        assertThat(logEventCaptor.getAllValues().stream()
                .filter(loggingEvent -> loggingEvent.getLevel() == Level.WARN &&
                        loggingEvent.getFormattedMessage().contains("best practice located at " + urlWithNoResource + " doesn't seem to exist"))
                .count()).isEqualTo(1);

    }

    @Test
    void shouldNotPostNotificationIfAlreadyThereFromPreviousEvent() {

        returnExistingComment("some comments about " + MATCHING_FILENAME);

        handler.handle(pullRequestEvent);

        verify(mockNotifier, never()).notify(any(User.class), messageCaptor.capture(), any(Map.class));
    }

    @Test
    void shouldPostOnlyForFilesOnwhichWeHaventCommentedYet() {

        String secondMatchingFileNameOnWhichTheresNoCommentYet = "commons/src/main/resources/db/changelog/changes/005-someOtherFile.yml";

        PullRequestFile secondMatchingPullRequestFile = new PullRequestFile();
        secondMatchingPullRequestFile.setFilename(secondMatchingFileNameOnWhichTheresNoCommentYet);

        when(mockRemoteSourceControl.fetchPullRequestFiles(REPO_FULL_NAME, 123))
                .thenReturn(asList(matchingPullRequestFile, secondMatchingPullRequestFile));

        returnExistingComment("some comments about " + MATCHING_FILENAME);

        handler.handle(pullRequestEvent);

        verify(mockNotifier, times(1)).notify(any(User.class), messageCaptor.capture(), any(Map.class));

        String commentPublished = messageCaptor.getValue().getContent();

        assertThat(commentPublished).contains(secondMatchingFileNameOnWhichTheresNoCommentYet);
        assertThat(commentPublished).doesNotContain(MATCHING_FILENAME);

    }

    private void returnExistingComment(String content) {
        PullRequestComment existingPrComment = new PullRequestComment(content,
                new User("someLogin", "firstName.lastName@domain.com"));

        List<PullRequestComment> existingPRcomments = singletonList(existingPrComment);
        when(mockRemoteSourceControl.fetchPullRequestComments(REPO_FULL_NAME, 123)).thenReturn(existingPRcomments);
    }


    private void returnMatchingPullRequestFileWhenFetchPullRequestFiles() {
        when(mockRemoteSourceControl.fetchPullRequestFiles(REPO_FULL_NAME, 123)).thenReturn(singletonList(matchingPullRequestFile));
    }

}