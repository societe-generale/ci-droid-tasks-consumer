package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class GithubEventListenerTest {

    private PushEventService mockPushEventService = mock(PushEventService.class);

    private PullRequestEventService mockPullRequestEventService =mock(PullRequestEventService.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private ClassLoader classLoader = GithubEventListenerTest.class.getClassLoader();

    private SourceControlEventListener listener;

    @BeforeEach
    public void setUp() {

        listener=new SourceControlEventListener(mockPullRequestEventService, mockPushEventService,new GitHubEventDeserializer());
    }

    @Test
    public void callsExpectedService_pushOnDefaultBranch() throws IOException {

        String pushEventPayload = IOUtils.toString(classLoader.getResourceAsStream("pushEvent.json"),"UTF-8");

        PushEvent pushEvent = objectMapper.readValue(pushEventPayload, GitHubPushEvent.class);

        listener.onPushEventOnDefaultBranch(pushEventPayload);

        verify(mockPushEventService,times(1)).onPushOnDefaultBranchEvent(pushEvent);
        verify(mockPullRequestEventService,never()).onPullRequestEvent(any(PullRequestEvent.class));

    }

    @Test
    public void callsExpectedService_pullRequestEvent() throws IOException {

        String pullRequestEventPayload = IOUtils.toString(classLoader.getResourceAsStream("pullRequestEvent.json"),"UTF-8");

        PullRequestEvent pullRequestEvent= objectMapper.readValue(pullRequestEventPayload, GitHubPullRequestEvent.class);

        listener.onPullRequestEvent(pullRequestEventPayload);

        verify(mockPushEventService,never()).onPushOnDefaultBranchEvent(any(PushEvent.class));
        verify(mockPullRequestEventService,times(1)).onPullRequestEvent(pullRequestEvent);
    }
}