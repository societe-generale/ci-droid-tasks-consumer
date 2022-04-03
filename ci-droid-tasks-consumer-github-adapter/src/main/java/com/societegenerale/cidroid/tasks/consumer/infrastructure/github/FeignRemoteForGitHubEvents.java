package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.config.GitHubConfig;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "GITHUB")
@FeignClient(name = "github-forEvents", url = "${source-control.url}", decode404 = true, configuration = GitHubConfig.class)
public interface FeignRemoteForGitHubEvents extends SourceControlEventsReactionPerformer {

    Map<String, String> bodyToClosePR = Collections.singletonMap("state", "closed");

    @GetMapping(value = "/repos/{repoFullName}/pulls?state=open",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    @Nonnull
    List<PullRequest> fetchOpenPullRequests(@PathVariable("repoFullName") String repoFullName);

    @GetMapping(value = "/repos/{repoFullName}/pulls/{prNumber}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    PullRequest fetchPullRequestDetails(@PathVariable("repoFullName") String repoFullName,
                                        @PathVariable("prNumber") int prNumber);

    @GetMapping(value = "/users/{login}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    User fetchUser(@PathVariable("login") String login);

    @PostMapping(value = "/repos/{repoFullName}/issues/{prNumber}/comments",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    void addCommentOnPR(@PathVariable("repoFullName") String repoFullName,
                                    @PathVariable("prNumber") int prNumber,
                                    @RequestBody Comment comment);

    @GetMapping(value = "/repos/{repoFullName}/pulls/{prNumber}/files",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    @Nonnull
    List<PullRequestFile> fetchPullRequestFiles(@PathVariable("repoFullName") String repoFullName,
                                                @PathVariable("prNumber") int prNumber);

    @GetMapping(value = "/repos/{repoFullName}/issues/{prNumber}/comments",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    @Nonnull
    List<PullRequestComment> fetchPullRequestComments(@PathVariable("repoFullName") String repoFullName,
                                                      @PathVariable("prNumber") int prNumber);

    @Override
    default void closePullRequest(String repoFullName, int prNumber) {
        updatePullRequest(repoFullName, prNumber, bodyToClosePR);
    }

    @PatchMapping(value = "/repos/{repoFullName}/pulls/{prNumber}",
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
