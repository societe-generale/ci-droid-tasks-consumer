package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.handlers;

import static com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus.MERGEABLE;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;

import com.societegenerale.cidroid.tasks.consumer.services.GitCommit;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;

class PullRequestRebaseIT extends SourceControlEventHandlerIT {

    private static final int PULL_REQUEST_ID = 1347;
    private static final String COMMIT_ID = "123456";
    private static final String COMMIT_MESSAGE = "a tiny commit that was done on master";

    @Autowired
    private Rebaser mockRebaser;

    @Test
    void shouldRebaseMergeablePullRequest() {
        PullRequest expectedPullRequest = argThat(pr -> pr.getNumber() == PULL_REQUEST_ID);
        when(mockRebaser.rebase(expectedPullRequest))
                .thenReturn(getRebaseResult());

        githubMockServer.updatePullRequestMergeabilityStatus(MERGEABLE);

        sourceControlEventListener.onPushEventOnDefaultBranch(rawGitHubPushEvent);

        String expectedComment = "CI-droid has rebased below 1 commit(s):\\n" +
                "- " + COMMIT_ID + " / " + COMMIT_MESSAGE + "\\n";

        gitHubMockClient.verify(
                request()
                        .withMethod("POST")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/issues/" + PULL_REQUEST_ID + "/comments")
                        .withBody("{\"body\":\"" + expectedComment + "\"}"),
                VerificationTimes.once()
        );
    }

    private Pair<com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest, List<GitCommit>> getRebaseResult() {
        GitCommit rebasedCommit = new GitCommit(COMMIT_ID, COMMIT_MESSAGE);
        return new ImmutablePair<>(pullRequest.toStandardPullRequest(), singletonList(rebasedCommit));
    }

}
