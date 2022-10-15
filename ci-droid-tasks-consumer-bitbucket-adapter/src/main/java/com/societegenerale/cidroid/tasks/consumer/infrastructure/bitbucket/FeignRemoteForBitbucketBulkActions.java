package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.config.BitbucketConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.*;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import feign.Feign;
import feign.Logger;
import feign.form.FormEncoder;
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

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@ConditionalOnProperty(prefix = "source-control", name = "type", havingValue = "BITBUCKET")
@FeignClient(name = "bitbucket-forBulkActions", url = "${source-control.url}", decode404 = true, configuration = BitbucketConfig.class)
public interface FeignRemoteForBitbucketBulkActions {

    @GetMapping(value = "/repos/{repoFullName}/pull-requests",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    // Todo why duplicate method?
    PullRequestWrapper fetchOpenPullRequests(@PathVariable("repoFullName") String repoFullName);

    default UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlPersonalToken)
            throws RemoteSourceControlAuthorizationException {

        return buildContentClient(repoFullName, path, sourceControlPersonalToken).deleteResource(directCommit);
    }


    @GetMapping(value = "/repos/{repositorySlug}/raw/{path}?at={branch}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    String fetchContent(@PathVariable("repositorySlug") String repoFullName,
                                 @PathVariable("path") String path,
                                 @PathVariable("branch") String branch);

    default UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlPersonalToken) throws
            RemoteSourceControlAuthorizationException {

        return buildContentClient(repoFullName, path, sourceControlPersonalToken).updateContent(directCommit);

    }

    static ContentClient buildContentClient(String repoFullName, String path, String sourceControlPersonalToken) {
        return Feign.builder()
                .logger(new Slf4jLogger(ContentClient.class))
                .encoder(new FormEncoder(new JacksonEncoder()))
                .decoder(new JacksonDecoder())
                .errorDecoder(new UpdateContentErrorDecoder())
                .requestInterceptor(new SourceControlApiAccessKeyInterceptor(sourceControlPersonalToken))
                .logLevel(Logger.Level.FULL)
                .target(ContentClient.class, BitbucketConfig.getBitbucket() + "/repos/" + repoFullName + "/browse/" + path);
    }

    @GetMapping(value = "/repos/{repoFullName}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    Optional<Repository> fetchRepository(@PathVariable("repoFullName") String repoFullName);

    @GetMapping(value = "/repos/{repoFullName}/commits/{branchName}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    Reference fetchHeadReferenceFrom(@PathVariable("repoFullName") String repoFullNameString, @PathVariable("branchName") String branchName);

    default Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlPersonalToken)
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        BitBucketReferenceClient bitbucketReferenceClient = BitBucketReferenceClient.buildBitbucketReferenceClient(sourceControlPersonalToken)
                .target(BitBucketReferenceClient.class, BitbucketConfig.getBitbucket() + "/repos/" + repoFullName + "branches");

        return bitbucketReferenceClient.createBranch(new InputRef(branchName, fromReferenceSha1));
    }

    // Todo cover this logic in IT test case
    default User fetchCurrentUser(String sourceControlPersonalToken, String emailAddress, String login) {
        String bitbucketUrl = BitbucketConfig.getBitbucket();
        String bitBucketUrlWithoutProjectCode = bitbucketUrl.substring(0, bitbucketUrl.lastIndexOf("/"));
        String bitBucketUrlWithoutProject = bitBucketUrlWithoutProjectCode.substring(0, bitBucketUrlWithoutProjectCode.lastIndexOf("/"));

        BitBucketReferenceClient bitbucketReferenceClient = BitBucketReferenceClient.buildBitbucketReferenceClient(sourceControlPersonalToken)
                .target(BitBucketReferenceClient.class, bitBucketUrlWithoutProject + "/users/" + login);

        return bitbucketReferenceClient.getCurrentUser();
    }

    default PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlPersonalToken)
            throws RemoteSourceControlAuthorizationException {

        BitBucketReferenceClient BitbucketReferenceClient = BitBucketReferenceClient.buildBitbucketReferenceClient(sourceControlPersonalToken)
                .target(BitBucketReferenceClient.class, BitbucketConfig.getBitbucket() + "/repos/" + repoFullName + "/pull-requests");

        return BitbucketReferenceClient.createPullRequest(newPr);
    }

    //
    @GetMapping(value = "/repos/{repositorySlug}/browse/{path}?at={branch}&blame=true&noContent=true",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Nonnull
    // Todo use limit to get one record
    List<Blame> fetchCommits(@PathVariable("repositorySlug") String repositorySlug, @PathVariable("path") String path,
                             @PathVariable("branch") String branch);

    @Data
    @AllArgsConstructor
    class InputRef {

        @JsonProperty("name")
        private String ref;

        @JsonProperty("startPoint")
        private String sha;

    }
}
