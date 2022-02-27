package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.GlobalProperties;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
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
@FeignClient(name = "github", url = "${source-control.url}", decode404 = true, configuration = RemoteGitHubConfig.class)
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



    static ContentClient buildContentClient(String repoFullName, String path, String oauthToken) {
        return Feign.builder()
                .logger(new Slf4jLogger(ContentClient.class))
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(new UpdateContentErrorDecoder())
                .requestInterceptor(new OAuthInterceptor(oauthToken))
                .logLevel(Logger.Level.FULL)
                .target(ContentClient.class, GlobalProperties.getGitHubApiUrl() + "/repos/" + repoFullName + "/contents/" + path);
    }

    @GetMapping(value = "/repos/{repoFullName}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    Optional<Repository> fetchRepository(@PathVariable("repoFullName") String repoFullName);

    @GetMapping(value = "/repos/{repoFullName}/git/refs/heads/{branchName}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    Reference fetchHeadReferenceFrom(@PathVariable("repoFullName") String repoFullNameString, @PathVariable("branchName") String branchName);


    @Override
    default User fetchCurrentUser(String oAuthToken) {

        GitReferenceClient gitReferenceClient = GitReferenceClient.buildGitReferenceClient(oAuthToken)
                .target(GitReferenceClient.class, GlobalProperties.getGitHubApiUrl() + "/user");

        return gitReferenceClient.getCurrentUser();

    }


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