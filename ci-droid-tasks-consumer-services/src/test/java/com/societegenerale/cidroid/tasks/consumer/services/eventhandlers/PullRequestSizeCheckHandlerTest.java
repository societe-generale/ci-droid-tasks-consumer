package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.util.List;
import java.util.Map;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.TestPullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class PullRequestSizeCheckHandlerTest {

    private final String REPO_FULL_NAME = "someOrg/someRepo";

    private final Notifier mockNotifier = mock(Notifier.class);

    private final SourceControlEventsReactionPerformer mockRemoteSourceControl = mock(SourceControlEventsReactionPerformer.class);

    private final int maxFilesInPR = 5;

    private final String maxFilesInPRExceededWarningMessage = "The PR should not have more than {0} files";

    private final PullRequestSizeCheckHandler handler = new PullRequestSizeCheckHandler(singletonList(mockNotifier),
            mockRemoteSourceControl, maxFilesInPR, maxFilesInPRExceededWarningMessage);

    private final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

    private final Repository repository = Repository.builder().fullName(REPO_FULL_NAME).build();

    private PullRequestEvent pullRequestEvent = new TestPullRequestEvent("created", 123, repository,"someRawEvent");


    @Test
    void shouldNotifyWithConfiguredContentWhenTheNumberOfFilesInPRExceedsConfiguredValue() {
        returnMoreThanConfiguredMaxFilesInPRFilesWhenFetchPullRequestFiles();

        handler.handle(pullRequestEvent);

        verify(mockNotifier, times(1)).notify(any(User.class), messageCaptor.capture(), any(Map.class));
        assertThat(messageCaptor.getValue().getContent()).contains("The PR should not have more than 5 files");

    }

    @Test
    void shouldNotCreateTheMaxFileInPRCommentIfAlreadyCommented() {
        returnMoreThanConfiguredMaxFilesInPRFilesWhenFetchPullRequestFiles();
        returnExistingComment("The PR should not have more than 5 files");

        handler.handle(pullRequestEvent);

        verify(mockNotifier, never()).notify(any(User.class), messageCaptor.capture(), any(Map.class));

    }

    private void returnExistingComment(String content) {
        PullRequestComment existingPrComment = new PullRequestComment(content,
                new User("someLogin", "firstName.lastName@domain.com"));

        List<PullRequestComment> existingPRcomments = singletonList(existingPrComment);
        when(mockRemoteSourceControl.fetchPullRequestComments(REPO_FULL_NAME, 123)).thenReturn(existingPRcomments);
    }


    private void returnMoreThanConfiguredMaxFilesInPRFilesWhenFetchPullRequestFiles() {
        when(mockRemoteSourceControl.fetchPullRequestFiles(REPO_FULL_NAME, 123)).thenReturn(
                asList(createPullRequestFile("MyClass1.java"), createPullRequestFile("MyClass2.java")
                        , createPullRequestFile("MyClass3.java"), createPullRequestFile("MyClass4.java")
                        , createPullRequestFile("MyClass5.java"), createPullRequestFile("MyClass6.java")));
    }

    private PullRequestFile createPullRequestFile(String fileName) {
        return PullRequestFile.builder()
                .filename(fileName)
                .build();

    }

}
