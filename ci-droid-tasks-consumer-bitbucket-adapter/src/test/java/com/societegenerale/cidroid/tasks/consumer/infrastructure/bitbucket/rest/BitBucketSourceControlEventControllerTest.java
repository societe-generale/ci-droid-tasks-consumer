package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.rest;

import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.readFileToString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BitBucketSourceControlEventController.class)
class BitBucketSourceControlEventControllerTest {


    @Autowired
    private MockMvc mvc;

    @MockBean
    private PushEventService mockPushEventService;

    @MockBean
    private PullRequestEventService mockPullRequestEventService;

    private final String pullRequestEventAsString = readFileToString("/pullRequestEvent.json");

    private final String pushEventAsString = readFileToString("/pushEvent.json");

    @Test
    void process_push_event_on_default_branch() throws Exception {

        performPOSTandExpectSuccess(pushEventAsString, "push");

        verify(mockPushEventService, times(1)).onPushOnDefaultBranchEvent(any(PushEvent.class));
    }

    @Test
    void process_push_event_on_non_default_branch() throws Exception {

        String pushEventOnNonDefaultBranchAsString = pushEventAsString.replaceAll("refs/heads/master", "refs/heads/someOtherBranch");

        performPOSTandExpectSuccess(pushEventOnNonDefaultBranchAsString, "push");

        verify(mockPushEventService, times(1)).onPushOnNonDefaultBranchEvent(any(PushEvent.class));
    }

    @Test
    void process_pull_request_event() throws Exception {

        performPOSTandExpectSuccess(pullRequestEventAsString, "pr:opened");

        verify(mockPullRequestEventService, times(1)).onPullRequestEvent(any(PullRequestEvent.class));
    }

    @Test
    void return_bad_request_for_unknown_event() throws Exception {

        String pullRequestEventAsString = readFileToString("/pullRequestEvent.json");

        String unknownEventAsString = pullRequestEventAsString.replace("\"eventKey\"", "\"UNKOWN_TYPE\"");

        mvc.perform(post("/cidroid-sync-webhook/bitbucket")
                        .header("X-Bitbucket-Event", "pr:unknown")
                        .contentType(APPLICATION_JSON)
                        .content(unknownEventAsString))
                .andExpect(status().is4xxClientError());
    }

    private void performPOSTandExpectSuccess(String requestBodyContent, String headerValues) throws Exception {
        mvc.perform(post("/cidroid-sync-webhook/bitbucket")
                        .header("X-Bitbucket-Event", headerValues)
                        .contentType(APPLICATION_JSON)
                        .content(requestBodyContent))
                .andExpect(status().is2xxSuccessful());
    }

}
