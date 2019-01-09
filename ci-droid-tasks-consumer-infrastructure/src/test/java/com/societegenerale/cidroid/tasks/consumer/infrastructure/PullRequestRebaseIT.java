package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.GitCommit;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PushEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer.GITHUB_MOCK_PORT;
import static com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus.MERGEABLE;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { InfraConfig.class, TestConfig.class },
        initializers = YamlFileApplicationContextInitializer.class)
public class PullRequestRebaseIT {

    private static final int PULL_REQUEST_ID = 1347;
    private static final String COMMIT_ID = "123456";
    private static final String COMMIT_MESSAGE = "a tiny commit that was done on master";

    @Autowired
    private GithubEventListener githubEventListener;

    @Autowired
    private Rebaser mockRebaser;

    @Autowired
    private GitHubMockServer githubMockServer;

    private PushEvent pushEvent;
    private PullRequest pullRequest;

    @Before
    public void setUp() throws IOException {
        githubMockServer.start();

        ObjectMapper objectMapper = new ObjectMapper();

        pushEvent = objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("pushEvent.json"),
                PushEvent.class);

        pullRequest = objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("singlePullRequest.json"),
                PullRequest.class);
    }

    @After
    public void tearDown() {
        githubMockServer.stop();
    }

    @Test
    public void shouldRebaseMergeablePullRequest() {
        PullRequest expectedPullRequest = argThat(pr -> pr.getNumber() == PULL_REQUEST_ID);
        when(mockRebaser.rebase(expectedPullRequest)).thenReturn(getRebaseResult());

        githubMockServer.updatePullRequestMergeabilityStatus(MERGEABLE);

        githubEventListener.onGitHubPushEventOnDefaultBranch(pushEvent);

        String expectedComment = "CI-droid has rebased below 1 commit(s):\\n" +
                "- " + COMMIT_ID + " / " + COMMIT_MESSAGE + "\\n";

        new MockServerClient("localhost", GITHUB_MOCK_PORT).verify(
                request()
                        .withMethod("POST")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/issues/" + PULL_REQUEST_ID + "/comments")
                        .withBody("{\"body\":\"" + expectedComment + "\"}"),
                VerificationTimes.once()
        );
    }

    private Pair<PullRequest, List<GitCommit>> getRebaseResult() {
        GitCommit rebasedCommit = new GitCommit(COMMIT_ID, COMMIT_MESSAGE);
        return new ImmutablePair<>(pullRequest, singletonList(rebasedCommit));
    }

}