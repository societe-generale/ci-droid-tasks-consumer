package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Map;

import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier.PULL_REQUEST;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NotificationsHandlerTest {

    private static final String SINGLE_PULL_REQUEST_JSON = "/singlePullRequest.json";

    private static final int PULL_REQUEST_ID = 1347;

    private final RemoteSourceControl mockRemoteSourceControl = mock(RemoteSourceControl.class);

    private final Notifier mockNotifier = mock(Notifier.class);

    private NotificationsHandler notificationsHandler;

    private PullRequest singlePr;

    private PushEvent pushEvent;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {

        String prAsString = readFromInputStream(getClass().getResourceAsStream(SINGLE_PULL_REQUEST_JSON));
        singlePr = objectMapper.readValue(prAsString, PullRequest.class);

        String pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/pushEvent.json"));
        pushEvent = objectMapper.readValue(pushEventPayload, GitHubPushEvent.class);

        when(mockRemoteSourceControl.fetchUser("octocat")).thenReturn(new User("octocat", "octocat@github.com"));

        notificationsHandler = new NotificationsHandler(mockRemoteSourceControl, singletonList(mockNotifier));
    }


    @Test
    public void shouldNotifyPRownerIFNotMergeable() {

        notificationsHandler.handle(pushEvent, singletonList(singlePr));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<Map> additionalInfosCaptor = ArgumentCaptor.forClass(Map.class);


        verify(mockNotifier, times(1)).notify(userCaptor.capture(), messageCaptor.capture(), additionalInfosCaptor.capture());

        assertThat(userCaptor.getValue().getLogin()).isEqualTo("octocat");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("octocat@github.com");

        assertThat(messageCaptor.getValue().getContent()).startsWith("PR " + PULL_REQUEST_ID + " is not mergeable following commit");

        PullRequest actualPr = (PullRequest) additionalInfosCaptor.getValue().get(PULL_REQUEST);

        assertThat(actualPr).isEqualTo(singlePr);
    }

    @Test
    public void should_not_NotifyPRownerIFMergeable() {

        singlePr.setMergeable(true);

        notificationsHandler.handle(pushEvent, singletonList(singlePr));

        verify(mockNotifier, never()).notify(any(User.class), any(Message.class), anyMap());
    }

}