package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PushEvent;
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

import static org.mockserver.model.HttpRequest.request;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { InfraConfig.class, TestConfig.class },
        initializers = YamlFileApplicationContextInitializer.class)
public class PullRequestCleaningIT {

    private static final int GITHUB_MOCK_PORT = 9900;
    private static final int PULL_REQUEST_ID = 1347;

    @Autowired
    private GithubEventListener githubEventListener;

    @Autowired
    private GitHubMockServer githubMockServer;

    private PushEvent pushEvent;

    @Before
    public void setUp() throws IOException {
        githubMockServer.start();

        pushEvent = new ObjectMapper().readValue(
                getClass().getClassLoader().getResourceAsStream("pushEvent.json"),
                PushEvent.class);
    }

    @After
    public void tearDown() {
        githubMockServer.stop();
    }

    @Test
    public void shouldCloseOldPullRequests() {
        githubEventListener.onGitHubPushEventOnDefaultBranch(pushEvent);

        new MockServerClient("localhost", GITHUB_MOCK_PORT).verify(
                request()
                        .withMethod("PATCH")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/" + PULL_REQUEST_ID)
                        .withBody("{\"state\":\"closed\"}"),
                VerificationTimes.once()
        );
    }

}