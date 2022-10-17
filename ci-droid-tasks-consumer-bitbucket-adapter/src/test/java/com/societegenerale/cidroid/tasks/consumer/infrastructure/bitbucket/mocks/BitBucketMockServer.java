package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.*;
import com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.mockserver.model.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.*;
import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.getUser;
import static com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus.NOT_MERGEABLE;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
public class BitBucketMockServer extends MockServer {

    public static final int BITBUCKET_MOCK_PORT = 9800;

    private final ObjectMapper objectMapper;

    public BitBucketMockServer() {
        super(BITBUCKET_MOCK_PORT);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void initRoutes() {
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/projects/public-project/repos/public-repo/pull-requests"))
                .respond(getOpenPullRequests());

        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/projects/public-project/repos/public-repo/pull-requests/[0-9]+"))
                .respond(request -> getPullRequest());

        mockServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/projects/public-project/repos/public-repo/pull-requests"))
                .respond(getPullRequest());


        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/users/sekhar"))
                .respond(getCurrentUser());

        mockServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/projects/public-project/repos/public-repo/pull-requests/[0-9]+/comment"))
                .respond(response().withStatusCode(200));

        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/projects/public-project/repos/public-repo/raw/Jenkinsfile")
                        .withQueryStringParameter("at", "newJavaImageForJenkinsBuild"))
                .respond(returnContent());

        mockServer
                .when(request()
                        .withMethod("PATCH")
                        .withPath("/api/projects/public-project/repos/public-repo/pull-requests/[0-9]+"))
                .respond(response().withStatusCode(200));
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/projects/public-project/repos/public-repo"))
                .respond(returnRepository());
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/projects/public-project/repos/public-repo/commits/master"))
                .respond(returnReference());
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/projects/public-project/repos/public-repo/browse/Jenkinsfile")
                        .withQueryStringParameter("at", "newJavaImageForJenkinsBuild")
                        .withQueryStringParameter("limit", "1")
                        .withQueryStringParameter("blame", "true")
                        .withQueryStringParameter("noContent", "true"))

                .respond(returnBlames());
        mockServer
                .when(request()
                        .withMethod("PUT")
                        .withPath("/api/projects/public-project/repos/public-repo/browse/Jenkinsfile"))
                .respond(returnUpdatedResource());
        mockServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/projects/public-project/repos/public-repo/branches"))
                .respond(returnReference());
    }

    private HttpResponse returnContent() {
        return response()
                .withBody("JDK1.8-20170718-121455-e2e6123")
                .withHeader("Content-Type", "application/json")
                .withStatusCode(200);
    }

    @SneakyThrows
    private HttpResponse returnRepository() {
        return response()
                .withBody(objectMapper.writeValueAsString(repository()))
                .withHeader("Content-Type", "application/json")
                .withStatusCode(200);
    }

    @SneakyThrows
    private HttpResponse returnReference() {
        return response()
                .withBody(objectMapper.writeValueAsString(reference()))
                .withHeader("Content-Type", "application/json")
                .withStatusCode(200);
    }
    @SneakyThrows
    private HttpResponse returnBlames() {
        return response()
                .withBody(objectMapper.writeValueAsString(List.of(new Blame(ZonedDateTime.now(), "commitHash"))))
                .withHeader("Content-Type", "application/json")
                .withStatusCode(200);
    }
    @SneakyThrows
    private HttpResponse returnUpdatedResource() {
        return response()
                .withBody(objectMapper.writeValueAsString(UpdatedResource.builder().id("1234").author(new User("sekhar", "some.mail@gmail.com")).build()))
                .withHeader("Content-Type", "application/json")
                .withStatusCode(200);
    }

    @SneakyThrows
    private HttpResponse getOpenPullRequests() {
        return response()
                .withBody(objectMapper.writeValueAsString(new PullRequestWrapper(List.of(pullRequest()))))
                .withHeader("Content-Type", "application/json");
    }

    @SneakyThrows
    private HttpResponse getPullRequest() {
        return response()
                .withBody(objectMapper.writeValueAsString(pullRequest()))
                .withHeader("Content-Type", "application/json");
    }

    @SneakyThrows
    private HttpResponse getUser() {
        return response()
                .withBody(objectMapper.writeValueAsString(new User("sekhar", "some.mail@gmail.com")))
                .withHeader("Content-Type", "application/json");
    }

    private HttpResponse getCurrentUser() {
        //reusing the same method - but we may need something specific for current user later
        return getUser();
    }
}
