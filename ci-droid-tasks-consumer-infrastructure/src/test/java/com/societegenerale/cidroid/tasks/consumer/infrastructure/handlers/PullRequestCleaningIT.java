package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;


import org.junit.jupiter.api.Test;
import org.mockserver.verify.VerificationTimes;

import static org.mockserver.model.HttpRequest.request;

public class PullRequestCleaningIT extends SourceControlEventHandlerIT {

    private static final int PULL_REQUEST_ID = 1347;

    @Test
    public void shouldCloseOldPullRequests() {
        sourceControlEventListener.onPushEventOnDefaultBranch(pushEvent);

        gitHubMockClient.verify(
                request()
                        .withMethod("PATCH")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/" + PULL_REQUEST_ID)
                        .withBody("{\"state\":\"closed\"}"),
                VerificationTimes.once()
        );
    }

}