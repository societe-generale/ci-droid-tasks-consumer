package com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.societegenerale.cidroid.tasks.consumer.services.model.github.PRmergeableStatus.NOT_MERGEABLE;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
public class GitHubMockServer {

    private ClientAndServer githubMockServer;
    private int port;
    private PRmergeableStatus pullRequestMergeableStatus;
    private ObjectMapper objectMapper;

    public GitHubMockServer(int port) {
        this.port = port;
        pullRequestMergeableStatus = NOT_MERGEABLE;
        objectMapper = new ObjectMapper();
    }

    public void start() {
        if (githubMockServer != null && githubMockServer.isRunning()) {
            return;
        }
        githubMockServer = startClientAndServer(port);
        initRoutes();
    }

    public void stop() {
        if (githubMockServer.isRunning()) {
            githubMockServer.stop();
        }
    }

    public void updatePullRequestMergeabilityStatus(PRmergeableStatus status) {
        pullRequestMergeableStatus = status;
    }

    private void initRoutes() {
        githubMockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls")
                        .withQueryStringParameter("state", "open"))
                .respond(getOpenPullRequests());

        githubMockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/[0-9]+"))
                .respond(request -> getPullRequest());

        githubMockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/users/baxterthehacker"))
                .respond(getUser());

        githubMockServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/issues/[0-9]+/comment"))
                .respond(response().withStatusCode(200));

        githubMockServer
                .when(request()
                        .withMethod("PATCH")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/[0-9]+"))
                .respond(response().withStatusCode(200));
    }

    private HttpResponse getOpenPullRequests() {
        return response()
                .withBody(readFromFile("pullRequests.json"))
                .withHeader("Content-Type", "application/json");
    }

    private HttpResponse getPullRequest() {
        String pullRequestAsString = getPullRequestWithMergeabilityStatus();

        return HttpResponse.response()
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

    private String readFromFile(String fileName) {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
        try {
            return IOUtils.toString(Objects.requireNonNull(resourceAsStream), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(String.format("The file %s does not exist", fileName));
        }
    }
}
