package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.config.GitHubConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes={ InfraConfig.class,TestConfig.class, GitHubConfig.class})
@ActiveProfiles("test")
/**
 * We're not supposed to have too much business logic in the class under test, but this can be useful to verify the payload received on remote github server side,
 * and/or how the feign client behaves depending on various response codes
 */
class FeignRemoteSourceControlTest {

    @Autowired
    private RemoteForGitHubBulkActionsWrapper remoteForGitHubBulkActionsWrapper;

    @Autowired
    private GitHubMockServer githubMockServer;

    @Autowired
    private ObjectMapper objectMapper;

    protected MockServerClient gitHubMockClient;

    @BeforeEach
    public void setUp(){
        githubMockServer.start();

        gitHubMockClient = new MockServerClient("localhost", GitHubMockServer.GITHUB_MOCK_PORT);

    }


    @AfterEach
    public void tearDown() {
        githubMockServer.stop();
    }

    @Test
    void PRtitleShouldBeReceivedAsSent() throws RemoteSourceControlAuthorizationException, JsonProcessingException {

        com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate prToCreate =
                com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate.builder()
                .base("master")
                .head("refs/heads/someFeatureBranch")
                .title("title containing a forward slash / that should be escaped")
                .body("Content of the PR")
                .build();

        remoteForGitHubBulkActionsWrapper.createPullRequest("baxterthehacker/public-repo", prToCreate, "someToken");

        gitHubMockClient.verify(
                HttpRequest.request()
                        .withBody(JsonBody.json(objectMapper.writeValueAsString(prToCreate))),
                VerificationTimes.exactly(1)
        );

    }


}
