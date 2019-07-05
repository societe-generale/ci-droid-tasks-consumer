package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.GitHubAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer.GITHUB_MOCK_PORT;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.JsonBody.json;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes={ InfraConfig.class,TestConfig.class}, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
/**
 * We're not supposed to have too much business logic in the class under test, but this can be useful to verify the payload received on remote github server side,
 * and/or how the feign client behaves depending on various response codes
 */
public class FeignRemoteGitHubTest {

    @Autowired
    private FeignRemoteGitHub feignRemoteGitHub;

    @Autowired
    private GitHubMockServer githubMockServer;

    @Autowired
    private ObjectMapper objectMapper;

    protected MockServerClient gitHubMockClient;

    @BeforeEach
    public void setUp(){
        githubMockServer.start();

        gitHubMockClient = new MockServerClient("localhost", GITHUB_MOCK_PORT);

    }


    @AfterEach
    public void tearDown() {
        githubMockServer.stop();
    }

    @Test
    public void PRtitleShouldBeReceivedAsSent() throws GitHubAuthorizationException, JsonProcessingException {

        PullRequestToCreate prToCreate= new PullRequestToCreate();
        prToCreate.setBase("master");
        prToCreate.setHead("refs/heads/someFeatureBranch");
        prToCreate.setTitle("title containing a forward slash / that should be escaped");
        prToCreate.setBody("Content of the PR");


        feignRemoteGitHub.createPullRequest("baxterthehacker/public-repo", prToCreate, "someToken");

        gitHubMockClient.verify(
                request()
                        .withBody(json(objectMapper.writeValueAsString(prToCreate))),
                VerificationTimes.exactly(1)
        );

    }


}