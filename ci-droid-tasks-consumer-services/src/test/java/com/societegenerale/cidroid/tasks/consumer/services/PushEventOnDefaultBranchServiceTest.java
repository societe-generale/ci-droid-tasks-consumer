package com.societegenerale.cidroid.tasks.consumer.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventOnDefaultBranchHandler;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus.NOT_MERGEABLE;
import static com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus.UNKNOWN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PushEventOnDefaultBranchServiceTest {

    private static final String FULL_REPO_NAME = "baxterthehacker/public-repo";

    private static final int PULL_REQUEST_ID = 1347;

    private static final String SINGLE_PULL_REQUEST_JSON = "/singlePullRequest.json";

    private final RemoteSourceControl mockRemoteSourceControl = mock(RemoteSourceControl.class);

    private final PushEventOnDefaultBranchHandler mockPushEventOnDefaultBranchHandler = mock(PushEventOnDefaultBranchHandler.class);

    private PushEventOnDefaultBranchService pushOnDefaultBranchService;

    private PushEvent pushEvent;

    private PullRequest singlePr;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    public void setUp() throws IOException {

        List<PushEventOnDefaultBranchHandler> pushEventOnDefaultBranchHandlers = new ArrayList<>();
        pushEventOnDefaultBranchHandlers.add(mockPushEventOnDefaultBranchHandler);

        pushOnDefaultBranchService = new PushEventOnDefaultBranchService(mockRemoteSourceControl, pushEventOnDefaultBranchHandlers);

        String pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/pushEvent.json"));
        pushEvent = objectMapper.readValue(pushEventPayload, GitHubPushEvent.class);


        String openPrsOnRepoAsString = readFromInputStream(getClass().getResourceAsStream("/pullRequests.json"));
        List<PullRequest> openPrsOnRepo = objectMapper.readValue(openPrsOnRepoAsString, new TypeReference<List<PullRequest>>() {
        });
        when(mockRemoteSourceControl.fetchOpenPullRequests(FULL_REPO_NAME)).thenReturn(openPrsOnRepo);

        String prAsString = readFromInputStream(getClass().getResourceAsStream(SINGLE_PULL_REQUEST_JSON));
        singlePr = objectMapper.readValue(prAsString, PullRequest.class);
        when(mockRemoteSourceControl.fetchPullRequestDetails(FULL_REPO_NAME, PULL_REQUEST_ID)).thenReturn(singlePr);

        when(mockRemoteSourceControl.fetchUser("octocat")).thenReturn(new User("octocat", "octocat@github.com"));

    }

    @Test
    public void runtimeExceptionInHandlerShouldNotPreventOthersToExecute() {

        List<PushEventOnDefaultBranchHandler> pushEventOnDefaultBranchHandlers = new ArrayList<>();

        PushEventOnDefaultBranchHandler mockPushEventOnDefaultBranchHandlerThrowingException = mock(PushEventOnDefaultBranchHandler.class);
        doThrow(RuntimeException.class).when(mockPushEventOnDefaultBranchHandlerThrowingException).handle(any(SourceControlEvent.class),anyList());


        pushEventOnDefaultBranchHandlers.add(mockPushEventOnDefaultBranchHandlerThrowingException);
        pushEventOnDefaultBranchHandlers.add(mockPushEventOnDefaultBranchHandler);

        pushOnDefaultBranchService = new PushEventOnDefaultBranchService(mockRemoteSourceControl, pushEventOnDefaultBranchHandlers);

        pushOnDefaultBranchService.onPushEvent(pushEvent);

        verify(mockPushEventOnDefaultBranchHandler,times(1)).handle(any(SourceControlEvent.class),anyList());

    }

    @Test
    public void shouldRequestAllOpenPRsWhenPushOnDefaultBranch() {

        pushOnDefaultBranchService.onPushEvent(pushEvent);

        verify(mockRemoteSourceControl, times(1)).fetchOpenPullRequests(FULL_REPO_NAME);
    }

    @Test
    public void shouldNotDoAnythingIfPushEventNotOnDefaultBranch() {

        pushEvent.setRef("someOtherBranch");

        pushOnDefaultBranchService.onPushEvent(pushEvent);

        verify(mockRemoteSourceControl, never()).fetchOpenPullRequests(any(String.class));
    }

    @Test
    public void shouldRequestOpenPRDetailsWhenPushOnDefaultBranch() {

        pushOnDefaultBranchService.onPushEvent(pushEvent);

        verify(mockRemoteSourceControl, times(1)).fetchPullRequestDetails("baxterthehacker/public-repo", PULL_REQUEST_ID);
    }

    @Test
    public void shouldApplyActionHandlersForRepository(){

        pushOnDefaultBranchService.onPushEvent(pushEvent);

        verify(mockPushEventOnDefaultBranchHandler,times(1)).handle(eq(pushEvent),anyList());

    }


    @Test
    public void shouldTrySeveralTimesToGetMergeableStatusIfNotAvailableImmediately() throws Exception {

        updatePRmergeabilityStatus(UNKNOWN);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                System.out.println("about to sleep...");
                TimeUnit.MILLISECONDS.sleep(700);
                System.out.println("done sleeping !");
                updatePRmergeabilityStatus(NOT_MERGEABLE);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println("submitted task to update PR merge status shortly...");

        pushOnDefaultBranchService.onPushEvent(pushEvent);

        ArgumentCaptor<List<PullRequest>> prListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<SourceControlEvent> gitHubEventCaptor = ArgumentCaptor.forClass(SourceControlEvent.class);

        await().atMost(3, SECONDS)
                .until(() -> verify(mockPushEventOnDefaultBranchHandler, times(1)).handle(gitHubEventCaptor.capture(), prListCaptor.capture()));

        assertThat(prListCaptor.getValue()).containsExactly(singlePr);
        assertThat(gitHubEventCaptor.getValue()).isEqualTo(pushEvent);

        verify(mockRemoteSourceControl, atLeast(2)).fetchPullRequestDetails("baxterthehacker/public-repo", PULL_REQUEST_ID);

        executor.shutdownNow();
    }

    private void updatePRmergeabilityStatus(PRmergeableStatus pRmergeableStatus) throws IOException {

        PullRequest pullRequest = objectMapper.readValue(readFromInputStream(getClass().getResourceAsStream(SINGLE_PULL_REQUEST_JSON)), PullRequest.class);
        pullRequest.setMergeable(pRmergeableStatus.getValue());

        when(mockRemoteSourceControl.fetchPullRequestDetails(FULL_REPO_NAME, PULL_REQUEST_ID)).thenReturn(pullRequest);
    }

}