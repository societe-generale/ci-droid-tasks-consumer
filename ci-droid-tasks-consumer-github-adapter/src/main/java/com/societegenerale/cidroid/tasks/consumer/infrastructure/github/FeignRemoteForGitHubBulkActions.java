package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.config.GitHubConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.config.GlobalProperties;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "GITHUB")
@FeignClient(name = "github-forBulkActions", url = "${source-control.url}", decode404 = true, configuration = GitHubConfig.class)
public interface FeignRemoteForGitHubBulkActions extends SourceControlBulkActionsPerformer {

    @GetMapping(value = "/repos/{repoFullName}/pulls?state=open",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    @Nonnull
    List<PullRequest> fetchOpenPullRequests(@PathVariable("repoFullName") String repoFullName);

    @Override
    default UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlPersonalToken)
            throws RemoteSourceControlAuthorizationException {

        return buildContentClient(repoFullName, path, sourceControlPersonalToken).deleteResource(directCommit);
    }


    @GetMapping(value = "/repos/{repoFullName}/contents/{path}?ref={branch}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    ResourceContent fetchContent(@PathVariable("repoFullName") String repoFullName,
                                 @PathVariable("path") String path,
                                 @PathVariable("branch") String branch);


    @Override
    default UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlPersonalToken) throws
            RemoteSourceControlAuthorizationException {

        return buildContentClient(repoFullName, path, sourceControlPersonalToken).updateContent(directCommit);

    }

    static ContentClient buildContentClient(String repoFullName, String path, String sourceControlPersonalToken) {
        return Feign.builder()
                .logger(new Slf4jLogger(ContentClient.class))
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(new UpdateContentErrorDecoder())
                .requestInterceptor(new SourceControlApiAccessKeyInterceptor(sourceControlPersonalToken))
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
    default Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlPersonalToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        GitReferenceClient gitReferenceClient = GitReferenceClient.buildGitReferenceClient(sourceControlPersonalToken)
                .target(GitReferenceClient.class, GlobalProperties.getGitHubApiUrl() + "/repos/" + repoFullName + "/git/refs");

        return gitReferenceClient.createBranch(new InputRef("refs/heads/" + branchName, fromReferenceSha1));
    }

    @Override
    default User fetchCurrentUser(String sourceControlPersonalToken, String emailAddress) {

        GitReferenceClient gitReferenceClient = GitReferenceClient.buildGitReferenceClient(sourceControlPersonalToken)
                .target(GitReferenceClient.class, GlobalProperties.getGitHubApiUrl() + "/user");

        return gitReferenceClient.getCurrentUser();

    }

    @Override
    default PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlPersonalToken)
            throws RemoteSourceControlAuthorizationException {

        GitReferenceClient gitReferenceClient = GitReferenceClient.buildGitReferenceClient(sourceControlPersonalToken)
                .target(GitReferenceClient.class, GlobalProperties.getGitHubApiUrl() + "/repos/" + repoFullName + "/pulls");

        return gitReferenceClient.createPullRequest(newPr);
    }

    @Data
    @AllArgsConstructor
    class InputRef {

        private String ref;

        private String sha;

    }
}
