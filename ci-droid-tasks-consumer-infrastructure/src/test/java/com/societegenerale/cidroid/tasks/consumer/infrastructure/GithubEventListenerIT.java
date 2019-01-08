package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMock;
import com.societegenerale.cidroid.tasks.consumer.services.GitCommit;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PushEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.jayway.awaitility.Awaitility.await;
import static com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus.MERGEABLE;
import static com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus.NOT_MERGEABLE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { InfraConfig.class, TestConfig.class },
        initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
public class GithubEventListenerIT {

    private final int PULL_REQUEST_ID=1347;

    static boolean hasGitHubMockServerStarted = false;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    GitHubMock githubMockServer;

    @Autowired
    GithubEventListener githubEventListener;

    @Autowired
    Rebaser mockRebaser;

    PushEvent pushEvent;

    @Before
    public void setup() throws IOException {
        String pushEventPayload = IOUtils
                .toString(GithubEventListenerIT.class.getClassLoader().getResourceAsStream("pushEvent.json"), "UTF-8");

        pushEvent = objectMapper.readValue(pushEventPayload, PushEvent.class);

    }

    @Before
    public void mockSetUp() {

        if (!hasGitHubMockServerStarted) {

            githubMockServer.start();

            await().atMost(5, SECONDS)
                    .until(() -> assertThat(GitHubMock.hasStarted()).isTrue());

            hasGitHubMockServerStarted = true;
        }

        githubMockServer.reset();

        githubMockServer.updatePRmergeabilityStatus(PULL_REQUEST_ID,NOT_MERGEABLE);
    }

    @After
    public void tearDown() {
        githubMockServer.stop();
        hasGitHubMockServerStarted = false;
    }

    @Test
    public void shouldRebaseMergeablePr() throws Exception {

        String prAsString = IOUtils
                .toString(GithubEventListenerIT.class.getClassLoader().getResourceAsStream("singlePullRequest.json"), "UTF-8");
        PullRequest singlePr = objectMapper.readValue(prAsString, PullRequest.class);

        List rebasedCommitsForOnePr = Arrays.asList(new GitCommit("123456", "a tiny commit that was done on master"));
        Pair<PullRequest, List<GitCommit>> rebaseResult = new ImmutablePair(singlePr, rebasedCommitsForOnePr);

        ArgumentMatcher<PullRequest> isExpectedPullRequest = pr -> pr.getNumber()==PULL_REQUEST_ID;

        when(mockRebaser.rebase(argThat(isExpectedPullRequest))).thenReturn(rebaseResult);


        githubMockServer.updatePRmergeabilityStatus(PULL_REQUEST_ID,MERGEABLE);

        githubEventListener.onGitHubPushEventOnDefaultBranch(pushEvent);

        await().atMost(2, SECONDS)
                .until(() -> assertThat(verify(mockRebaser,times(1)).rebase(any(PullRequest.class))));

        assertThat(githubMockServer.getCommentsPerPr().get(Integer.toString(PULL_REQUEST_ID))).isNotNull();

    }



}