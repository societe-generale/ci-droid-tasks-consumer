package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.TestConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.YamlFileApplicationContextInitializer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
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
public abstract class ActionToPerformEventHandlerIT {

    @Autowired
    protected ActionToPerformListener actionToPerformListener;

    @Autowired
    private GitHubMockServer githubMockServer;

    protected MockServerClient gitHubMockClient;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp(){
        githubMockServer.start();

        gitHubMockClient = new MockServerClient("localhost", GITHUB_MOCK_PORT);

    }

    @AfterEach
    public void tearDown() {
        githubMockServer.stop();
    }

    protected Object getObjectFromJson(String fileName, Class<?> clazz) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(fileName), clazz);
    }

}