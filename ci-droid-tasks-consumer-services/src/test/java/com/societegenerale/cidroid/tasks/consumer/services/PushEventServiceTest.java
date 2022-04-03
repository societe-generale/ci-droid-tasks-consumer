package com.societegenerale.cidroid.tasks.consumer.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus.NOT_MERGEABLE;
import static com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus.UNKNOWN;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PushEventServiceTest {

    private static final String FULL_REPO_NAME = "baxterthehacker/public-repo";

    private static final int PULL_REQUEST_ID = 1347;

    private static final String SINGLE_PULL_REQUEST_JSON = "/singlePullRequest.json";

    private final SourceControlEventsReactionPerformer mockRemoteSourceControl = mock(SourceControlEventsReactionPerformer.class);

    private final PushEventHandler mockPushEventHandler = mock(PushEventHandler.class);

    private PushEventService pushOnDefaultBranchService;

    private PushEvent pushEvent;

    private PullRequest singlePr;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    public void setUp() throws IOException {

        List<PushEventHandler> pushEventHandlers = new ArrayList<>();
        pushEventHandlers.add(mockPushEventHandler);

        pushOnDefaultBranchService = new PushEventService(mockRemoteSourceControl, pushEventHandlers,false,null);

        String pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/pushEvent.json"));
        pushEvent = objectMapper.readValue(pushEventPayload, PushEvent.class);


        String openPrsOnRepoAsString = readFromInputStream(getClass().getResourceAsStream("/pullRequests.json"));
        List<PullRequest> openPrsOnRepo = objectMapper.readValue(openPrsOnRepoAsString, new TypeReference<>() {
        });
        when(mockRemoteSourceControl.fetchOpenPullRequests(FULL_REPO_NAME)).thenReturn(openPrsOnRepo);

        String prAsString = readFromInputStream(getClass().getResourceAsStream(SINGLE_PULL_REQUEST_JSON));
        singlePr = objectMapper.readValue(prAsString, PullRequest.class);
        when(mockRemoteSourceControl.fetchPullRequestDetails(FULL_REPO_NAME, PULL_REQUEST_ID)).thenReturn(singlePr);

        when(mockRemoteSourceControl.fetchUser("octocat")).thenReturn(new User("octocat", "octocat@github.com"));

    }

    @Test
    void monitoringConfigMustBeConsistent() {

        assertThat(catchThrowable(() -> new PushEventService(mockRemoteSourceControl, emptyList(),true,null) ))
                .as("if monitoring is enabled, then pushEventMonitor can't be null")
                .isInstanceOf(IllegalStateException.class);

        assertThatCode(() ->
            new PushEventService(mockRemoteSourceControl, emptyList(),true,mock(PushEventMonitor.class))
        ).doesNotThrowAnyException();

        assertThatCode(() -> new PushEventService(mockRemoteSourceControl, emptyList(),false,mock(PushEventMonitor.class)))
            .as("even when a pushEventMonitor is provided, it can be disabled with a flag")
                .doesNotThrowAnyException();
    }


    @Test
    void runtimeExceptionInHandlerShouldNotPreventOthersToExecute() {

        List<PushEventHandler> pushEventHandlers = new ArrayList<>();

        PushEventHandler mockPushEventHandlerThrowingException = mock(PushEventHandler.class);
        doThrow(RuntimeException.class).when(mockPushEventHandlerThrowingException).handle(any(SourceControlEvent.class),anyList());


        pushEventHandlers.add(mockPushEventHandlerThrowingException);
        pushEventHandlers.add(mockPushEventHandler);

        pushOnDefaultBranchService = new PushEventService(mockRemoteSourceControl, pushEventHandlers,false,null);

        pushOnDefaultBranchService.onPushOnDefaultBranchEvent(pushEvent);

        verify(mockPushEventHandler,times(1)).handle(any(SourceControlEvent.class),anyList());

    }

    @Test
    void shouldRequestAllOpenPRsWhenPushOnDefaultBranch() {

        pushOnDefaultBranchService.onPushOnDefaultBranchEvent(pushEvent);

        verify(mockRemoteSourceControl, times(1)).fetchOpenPullRequests(FULL_REPO_NAME);
    }

    @Test
    void shouldNotDoAnythingIfPushEventNotOnDefaultBranch() {

        pushEvent.setRef("someOtherBranch");

        pushOnDefaultBranchService.onPushOnDefaultBranchEvent(pushEvent);

        verify(mockRemoteSourceControl, never()).fetchOpenPullRequests(any(String.class));
    }

    @Test
    void shouldRequestOpenPRDetailsWhenPushOnDefaultBranch() {

        pushOnDefaultBranchService.onPushOnDefaultBranchEvent(pushEvent);

        verify(mockRemoteSourceControl, times(1)).fetchPullRequestDetails("baxterthehacker/public-repo", PULL_REQUEST_ID);
    }

    @Test
    void shouldApplyActionHandlersForRepository(){

        pushOnDefaultBranchService.onPushOnDefaultBranchEvent(pushEvent);

        verify(mockPushEventHandler,times(1)).handle(eq(pushEvent),anyList());

    }


    @Test
    void shouldTrySeveralTimesToGetMergeableStatusIfNotAvailableImmediately() throws Exception {

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

        pushOnDefaultBranchService.onPushOnDefaultBranchEvent(pushEvent);

        ArgumentCaptor<List<PullRequest>> prListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<SourceControlEvent> gitHubEventCaptor = ArgumentCaptor.forClass(SourceControlEvent.class);

        verify(mockPushEventHandler, timeout(3000)).handle(gitHubEventCaptor.capture(), prListCaptor.capture());

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
