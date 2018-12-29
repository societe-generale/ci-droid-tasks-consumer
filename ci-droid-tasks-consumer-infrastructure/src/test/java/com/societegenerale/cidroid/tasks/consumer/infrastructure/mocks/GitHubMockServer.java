package com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

@Slf4j
public class GitHubMockServer {

    private ClientAndServer githubMockServer;

    private int port;

    public GitHubMockServer(int port) {
        this.port = port;
    }

    public void start() {
        if (githubMockServer != null && githubMockServer.isRunning()) {
            return;
        }
        githubMockServer = startClientAndServer(port);
        initRoutes();
    }

    public void stop() {
        githubMockServer.stop();
    }

    private void initRoutes() {
        githubMockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls")
                        .withQueryStringParameter("status", "open"))
                .respond(getOpenPullRequests());

        githubMockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/repos/baxterthehacker/public-repo/pulls/[0-9]+"))
                .respond(getPullRequest());

        githubMockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/v3/users/baxterthehacker"))
                .respond(getUser());
    }

    private HttpResponse getOpenPullRequests() {
        return HttpResponse.response()
                .withBody(readFromFile("pullRequests.json"))
                .withHeader("Content-Type", "application/json");
    }

    private HttpResponse getPullRequest() {
        return HttpResponse.response()
                .withBody(readFromFile("singlePullRequest.json"))
                .withHeader("Content-Type", "application/json");
    }

    private HttpResponse getUser() {
        return HttpResponse.response()
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
