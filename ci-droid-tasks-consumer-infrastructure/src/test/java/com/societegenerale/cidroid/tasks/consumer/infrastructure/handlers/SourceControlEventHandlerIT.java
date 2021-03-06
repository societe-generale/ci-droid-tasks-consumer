package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.TestConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.YamlFileApplicationContextInitializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.AsyncConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer.GITHUB_MOCK_PORT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { InfraConfig.class, AsyncConfig.class,TestConfig.class },
        initializers = YamlFileApplicationContextInitializer.class)
public abstract class SourceControlEventHandlerIT {

    @Autowired
    protected SourceControlEventListener sourceControlEventListener;

    @Autowired
    protected GitHubMockServer githubMockServer;

    MockServerClient gitHubMockClient;

    String rawGitHubPushEvent;
    String rawGitHubPullRequest;

    PushEvent pushEvent;
    PullRequest pullRequest;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        githubMockServer.start();

        gitHubMockClient = new MockServerClient("localhost", GITHUB_MOCK_PORT);

        rawGitHubPushEvent = readFile("pushEvent.json");
        rawGitHubPullRequest = readFile("singlePullRequest.json");

        pushEvent = (PushEvent) getObjectFromJson("pushEvent.json", GitHubPushEvent.class);
        pullRequest = (PullRequest) getObjectFromJson("singlePullRequest.json", PullRequest.class);
    }

    @AfterEach
    public void tearDown() {
        githubMockServer.stop();
    }


    private String readFile(String filename ) throws IOException {

        return IOUtils
                .toString(SourceControlEventHandlerIT.class.getClassLoader().getResourceAsStream(filename), "UTF-8");
    }

    private Object getObjectFromJson(String fileName, Class<?> clazz) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(fileName), clazz);
    }

}
