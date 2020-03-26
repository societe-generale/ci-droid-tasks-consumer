package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventOnDefaultBranchService;
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

    private PushEventOnDefaultBranchService mockPushOnDefaultBranchService = mock(PushEventOnDefaultBranchService.class);

    private PullRequestEventService mockPullRequestEventService =mock(PullRequestEventService.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private ClassLoader classLoader = GithubEventListenerTest.class.getClassLoader();

    private GithubEventListener listener;

    @BeforeEach
    public void setUp() {

        listener=new GithubEventListener(mockPushOnDefaultBranchService, mockPullRequestEventService);
    }

    @Test
    public void callsExpectedService_pushOnDefaultBranch() throws IOException {

        String pushEventPayload = IOUtils.toString(classLoader.getResourceAsStream("pushEvent.json"),"UTF-8");

        PushEvent pushEvent = objectMapper.readValue(pushEventPayload, GitHubPushEvent.class);

        listener.onGitHubPushEventOnDefaultBranch(pushEvent);

        verify(mockPushOnDefaultBranchService,times(1)).onGitHubPushEvent(pushEvent);
        verify(mockPullRequestEventService,never()).onGitHubPullRequestEvent(any(PullRequestEvent.class));

    }

    @Test
    public void callsExpectedService_pullRequestEvent() throws IOException {

        String pullRequestEventPayload = IOUtils.toString(classLoader.getResourceAsStream("pullRequestEvent.json"),"UTF-8");

        PullRequestEvent pullRequestEvent= objectMapper.readValue(pullRequestEventPayload, GitHubPullRequestEvent.class);


        listener.onGitHubPullRequestEvent(pullRequestEvent);

        verify(mockPushOnDefaultBranchService,never()).onGitHubPushEvent(any(PushEvent.class));
        verify(mockPullRequestEventService,times(1)).onGitHubPullRequestEvent(pullRequestEvent);
    }
}