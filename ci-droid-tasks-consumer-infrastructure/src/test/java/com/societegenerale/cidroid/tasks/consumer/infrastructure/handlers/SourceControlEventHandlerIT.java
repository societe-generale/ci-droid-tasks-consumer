package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.GithubEventListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.TestConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.YamlFileApplicationContextInitializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer.GITHUB_MOCK_PORT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { InfraConfig.class, TestConfig.class },
        initializers = YamlFileApplicationContextInitializer.class)
public abstract class SourceControlEventHandlerIT {

    @Autowired
    protected GithubEventListener githubEventListener;

    @Autowired
    protected GitHubMockServer githubMockServer;

    MockServerClient gitHubMockClient;

    PushEvent pushEvent;
    PullRequest pullRequest;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        githubMockServer.start();

        gitHubMockClient = new MockServerClient("localhost", GITHUB_MOCK_PORT);

        pushEvent = (PushEvent) getObjectFromJson("pushEvent.json", GitHubPushEvent.class);
        pullRequest = (PullRequest) getObjectFromJson("singlePullRequest.json", PullRequest.class);
    }

    @AfterEach
    public void tearDown() {
        githubMockServer.stop();
    }

    private Object getObjectFromJson(String fileName, Class<?> clazz) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(fileName), clazz);
    }

}