package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;

import static org.mockserver.model.HttpRequest.request;

import org.junit.jupiter.api.Test;
import org.mockserver.verify.VerificationTimes;

class PullRequestCleaningIT extends SourceControlEventHandlerIT {

    private static final int PULL_REQUEST_ID = 1347;

    @Test
    void shouldCloseOldPullRequests() {
        sourceControlEventListener.onPushEventOnDefaultBranch(rawGitHubPushEvent);

        gitHubMockClient.verify(
                request()
                        .withMethod("PATCH")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/" + PULL_REQUEST_ID)
                        .withBody("{\"state\":\"closed\"}"),
                VerificationTimes.once()
        );
    }
}