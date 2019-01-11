package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.GithubEventListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.TestConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.YamlFileApplicationContextInitializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PushEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer.GITHUB_MOCK_PORT;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { InfraConfig.class, TestConfig.class },
        initializers = YamlFileApplicationContextInitializer.class)
public abstract class GitHubEventHandlerIT {

    @Autowired
    protected GithubEventListener githubEventListener;

    @Autowired
    protected GitHubMockServer githubMockServer;

    MockServerClient gitHubMockClient;

    PushEvent pushEvent;
    PullRequest pullRequest;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws IOException {
        githubMockServer.start();

        gitHubMockClient = new MockServerClient("localhost", GITHUB_MOCK_PORT);

        pushEvent = (PushEvent) getObjectFromJson("pushEvent.json", PushEvent.class);
        pullRequest = (PullRequest) getObjectFromJson("singlePullRequest.json", PullRequest.class);
    }

    @After
    public void tearDown() {
        githubMockServer.stop();
    }

    private Object getObjectFromJson(String fileName, Class<?> clazz) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(fileName), clazz);
    }

}