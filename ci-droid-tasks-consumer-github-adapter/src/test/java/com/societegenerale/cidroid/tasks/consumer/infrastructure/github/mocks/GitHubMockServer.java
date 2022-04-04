package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.mocks;

import static com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus.NOT_MERGEABLE;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.mockserver.model.HttpResponse;

@Slf4j
public class GitHubMockServer extends MockServer {

    public static final int GITHUB_MOCK_PORT = 9900;

    private PRmergeableStatus pullRequestMergeableStatus;
    private ObjectMapper objectMapper;

    public GitHubMockServer() {
        super(GITHUB_MOCK_PORT);
        pullRequestMergeableStatus = NOT_MERGEABLE;
        objectMapper = new ObjectMapper();
    }

    public void updatePullRequestMergeabilityStatus(PRmergeableStatus status) {
        pullRequestMergeableStatus = status;
    }

    @Override
    protected void initRoutes() {
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls")
                        .withQueryStringParameter("state", "open"))
                .respond(getOpenPullRequests());

        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/[0-9]+"))
                .respond(request -> getPullRequest());

        mockServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls"))
                .respond(getPullRequestCreationResponse());


        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/users/baxterthehacker"))
                .respond(getUser());

        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/user"))
                .respond(getCurrentUser());

        mockServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/issues/[0-9]+/comment"))
                .respond(response().withStatusCode(200));

        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/repos/myOrga/myRepo/contents/JenkinsFileQuality")
                        .withQueryStringParameter("ref","master"))
                .respond(returnContent());

        mockServer
                .when(request()
                        .withMethod("PATCH")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/[0-9]+"))
                .respond(response().withStatusCode(200));
    }

    private HttpResponse getPullRequestCreationResponse() {

        return response()
            .withBody(readFromFile("pullRequestCreationResponse.json"))
            .withStatusCode(201);

    }

    private HttpResponse returnContent() {

        ResourceContent existingResourceContent=new ResourceContent();
        existingResourceContent.setSha("someSHA");

        HttpResponse response= null;

        try {
            response = response()
                    .withBody(objectMapper.writeValueAsString(existingResourceContent))
                    .withHeader("Content-Type", "application/json")
                    .withStatusCode(200);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("can't build the response - failing the test");
        }

        return response;
    }

    private HttpResponse getOpenPullRequests() {
        return response()
                .withBody(readFromFile("pullRequests.json"))
                .withHeader("Content-Type", "application/json");
    }

    private HttpResponse getPullRequest() {
        String pullRequestAsString = getPullRequestWithMergeabilityStatus();

        return response()
                .withBody(pullRequestAsString)
                .withHeader("Content-Type", "application/json");
    }

    private String getPullRequestWithMergeabilityStatus() {
        try {
            PullRequest pullRequest = objectMapper.readValue(
                    getClass().getClassLoader().getResourceAsStream("singlePullRequest.json"),
                    PullRequest.class);

            pullRequest.setMergeable(pullRequestMergeableStatus.getValue());

            return objectMapper.writeValueAsString(pullRequest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem reading or writing the pull request");
        }
    }

    private HttpResponse getUser() {
        return response()
                .withBody(readFromFile("user.json"))
                .withHeader("Content-Type", "application/json");
    }

    private HttpResponse getCurrentUser() {
        //reusing the same method - but we may need something specific for current user later
        return getUser();
    }


    private String readFromFile(String fileName) {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
        try {
            return IOUtils.toString(Objects.requireNonNull(resourceAsStream), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(String.format("The file %s does not exist", fileName));
        }
    }
}
