package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.TestUtils;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(SourceControlEventController.class)
class SourceControlEventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PushEventService mockPushEventService;

    @MockBean
    private PullRequestEventService mockPullRequestEventService;

    private String pushEventAsString= TestUtils.readFileToString("/gitLab/pushEventGitLab.json");

    private String pullRequestEventAsString= TestUtils.readFileToString("/gitLab/mergeRequestEventGitLab.json");

    @Test
    void forwardEventToNonDefaultBranchProcessing_whenPushOnNonDefaultBranch() throws Exception {

        String pushEventOnBranchAsString = pushEventAsString.replaceAll("refs/heads/master", "refs/heads/someOtherBranch");

        performPOSTandExpectSuccess(pushEventOnBranchAsString, "Push Hook");

        verify(mockPushEventService, times(1)).onPushOnNonDefaultBranchEvent(any(PushEvent.class));

    }

    @Test
    void forwardEventToDefaultBranchProcessing_whenPushOnDefaultBranch() throws Exception {

        performPOSTandExpectSuccess(pushEventAsString, "Push Hook");

        verify(mockPushEventService, times(1)).onPushOnDefaultBranchEvent(any(PushEvent.class));

    }

    @Test
    void forwardEventToPullRequestProcessing_whenPullRequestEvent() throws Exception {

        performPOSTandExpectSuccess(pullRequestEventAsString, "Merge Request Hook");

        verify(mockPullRequestEventService, times(1)).onPullRequestEvent(any(PullRequestEvent.class));

    }

    private void performPOSTandExpectSuccess(String requestBodyContent, String headerValues) throws Exception {
        mvc.perform(post("/cidroid-sync-webhook/gitlab")
                .header("X-Gitlab-Event", headerValues)
                .contentType(APPLICATION_JSON)
                .content(requestBodyContent))
                .andExpect(status().is2xxSuccessful());
    }

}

