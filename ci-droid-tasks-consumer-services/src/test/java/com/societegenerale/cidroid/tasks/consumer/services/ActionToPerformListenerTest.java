package com.societegenerale.cidroid.tasks.consumer.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.api.gitHubInteractions.DirectPushGitHubInteraction;
import com.societegenerale.cidroid.extensions.actionToReplicate.OverwriteStaticFileAction;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActionToPerformListenerTest {

    private final ActionToReplicate overWriteStaticContentAction = new OverwriteStaticFileAction();

    private final ActionToReplicate mockSomeOtherAction = mock(ActionToReplicate.class);

    private final ActionToPerformService mockActionToPerformService = mock(ActionToPerformService.class);

    private final SourceControlBulkActionsPerformer mockRemoteSourceControl = mock(SourceControlBulkActionsPerformer.class);

    private final ActionNotifier mockNotifier = mock(ActionNotifier.class);

    private ActionToPerformListener actionToPerformListener = new ActionToPerformListener(mockActionToPerformService,
                                                                                            Arrays.asList(overWriteStaticContentAction, mockSomeOtherAction),
                                                                                            mockNotifier);

    private final ArgumentCaptor<BulkActionToPerform> bulkActionToPerformCaptor = ArgumentCaptor.forClass(BulkActionToPerform.class);

    private ActionToPerformCommand incomingCommand;

    @BeforeEach
    public void setUp() throws IOException {

        String incomingCommandAsString = IOUtils
                .toString(ActionToPerformListenerTest.class.getClassLoader().getResourceAsStream("incomingOverWriteStaticContentAction.json"),
                        StandardCharsets.UTF_8);
        incomingCommand = new ObjectMapper().readValue(incomingCommandAsString, ActionToPerformCommand.class);

        when(mockRemoteSourceControl.fetchCurrentUser("someToken",null)).thenReturn(new User("someUserName","someEmail"));

    }

    @Test
    void shouldMapCorrectlyIncomingCommand() {
        //postConstruct
        actionToPerformListener.registerActionsToReplicate();

        actionToPerformListener.onActionToPerform(incomingCommand);

        verify(mockActionToPerformService, times(1)).perform(bulkActionToPerformCaptor.capture());

        BulkActionToPerform actualBulkActionToPerform = bulkActionToPerformCaptor.getValue();

        //assertThat(actualBulkActionToPerform.getUserRequestingAction().getLogin()).isEqualTo("someUserName");
        assertThat(actualBulkActionToPerform.getSourceControlPersonalToken()).isEqualTo("someToken");
        assertThat(actualBulkActionToPerform.getEmail()).isEqualTo("someEmail@someDomain.com");

        assertThat(actualBulkActionToPerform.getActionToReplicate()).isInstanceOf(OverwriteStaticFileAction.class);
        OverwriteStaticFileAction actualActionToReplicate = (OverwriteStaticFileAction) actualBulkActionToPerform.getActionToReplicate();
        assertThat(actualActionToReplicate.getStaticContent()).isEqualTo("some new content");

        assertThat(actualBulkActionToPerform.getGitHubInteraction()).isInstanceOf(DirectPushGitHubInteraction.class);
    }

    @Test
    void shouldNotifyIfUnexpectedExceptionDuringDeserialization_and_notPerformAnything() {

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> notificationBodyCaptor = ArgumentCaptor.forClass(String.class);

        //only one action registered, not matching the one
        actionToPerformListener = new ActionToPerformListener(mockActionToPerformService, List.of(mockSomeOtherAction), mockNotifier);

        actionToPerformListener.registerActionsToReplicate();

        actionToPerformListener.onActionToPerform(incomingCommand);

        //not performing anything
        verify(mockActionToPerformService, never()).perform(bulkActionToPerformCaptor.capture());

        //checking notification
        verify(mockNotifier, times(1)).notify(userCaptor.capture(),eq("[KO] unexpected error when request received, before 'core processing' actually happened"),notificationBodyCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("someEmail@someDomain.com");
        assertThat(notificationBodyCaptor.getValue()).contains("Exception");
    }


}
