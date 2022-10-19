package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.config.BitbucketConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "BITBUCKET")
@FeignClient(name = "bitbucket-forEvents", url = "${source-control.url}", decode404 = true, configuration = BitbucketConfig.class)
public interface FeignRemoteForBitbucketEvents {

    Map<String, String> bodyToClosePR = Collections.singletonMap("state", "closed");

    @GetMapping(value = "/repos/{repoFullName}/pull-requests",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    PullRequestWrapper fetchOpenPullRequests(@PathVariable("repoFullName") String repoFullName);

    @GetMapping(value = "/repos/{repoFullName}/pull-requests/{prNumber}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    PullRequest fetchPullRequestDetails(@PathVariable("repoFullName") String repoFullName,
                                        @PathVariable("prNumber") int prNumber);

    @GetMapping(value = "/users/{login}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    User fetchUser(@PathVariable("login") String login);

    @PostMapping(value = "/repos/{repoFullName}/pull-requests/{prNumber}/comments",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    void addCommentOnPR(@PathVariable("repoFullName") String repoFullName,
                                    @PathVariable("prNumber") int prNumber,
                                    @RequestBody Comment comment);

    @GetMapping(value = "/repos/{repoFullName}/pull-requests/{prNumber}/changes",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Nonnull
    PullRequestChange fetchPullRequestFiles(@PathVariable("repoFullName") String repoFullName,
                                                @PathVariable("prNumber") int prNumber);

    @GetMapping(value = "/repos/{repoFullName}/pull-requests/{prNumber}/activities",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Nonnull
    PullRequestActivity fetchPullRequestComments(@PathVariable("repoFullName") String repoFullName,
                                                      @PathVariable("prNumber") int prNumber);

    default void closePullRequest(String repoFullName, int prNumber) {
        updatePullRequest(repoFullName, prNumber, bodyToClosePR);
    }

    @PatchMapping(value = "/repos/{repoFullName}/pull-requests/{prNumber}",
                  consumes = MediaType.APPLICATION_JSON_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE)
    void updatePullRequest(@PathVariable("repoFullName") String repoFullName,
                           @PathVariable("prNumber") int prNumber,
                           @RequestBody Map<String, String> body);

    @Data
    @AllArgsConstructor
    class InputRef {

        private String ref;

        private String sha;

    }
}
