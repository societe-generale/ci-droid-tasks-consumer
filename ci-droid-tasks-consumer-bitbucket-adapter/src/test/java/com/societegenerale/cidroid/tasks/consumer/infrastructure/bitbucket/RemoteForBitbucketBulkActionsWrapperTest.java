package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.Blame;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.Project;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RemoteForBitbucketBulkActionsWrapperTest {

    private FeignRemoteForBitbucketBulkActions feignRemoteForBitbucketBulkActions = mock(FeignRemoteForBitbucketBulkActions.class);
    RemoteForBitbucketBulkActionsWrapper remoteForBitbucketBulkActionsWrapper = new RemoteForBitbucketBulkActionsWrapper(feignRemoteForBitbucketBulkActions, "CI-Project");

    @Test
    void should_fetch_encoded_content_and_latest_commit_id() {
        when(feignRemoteForBitbucketBulkActions.fetchContent("CI-Repo", "jenkin", "master")).thenReturn("Raw content");
        ZonedDateTime now = ZonedDateTime.now();
        when(feignRemoteForBitbucketBulkActions.fetchCommits("CI-Repo", "jenkin", "master"))
                .thenReturn(List.of(new Blame(now, "commitHashToday"),new Blame(now.minusDays(1), "commitHashYesterday"),
                        new Blame(now.minusDays(2), "commitHash2daysBefore")
                        ));
        ResourceContent resourceContent = remoteForBitbucketBulkActionsWrapper.fetchContent("CI-Repo", "jenkin", "master");
        assertThat(resourceContent.getBase64EncodedContent()).isEqualTo("UmF3IGNvbnRlbnQ=");
        assertThat(resourceContent.getSha()).isEqualTo("commitHashToday");
    }

    @Test
    void should_update_content_and_return_post_push_commit() throws RemoteSourceControlAuthorizationException {
        when(feignRemoteForBitbucketBulkActions.updateContent("CI-Repo", "jenkin", DirectCommit.from(directCommit()), "Token"))
                .thenReturn(UpdatedResource.builder().id("1234").author(getUser()).build());
        var resourceContent = remoteForBitbucketBulkActionsWrapper.updateContent("CI-Repo", "jenkin", directCommit(), "Token");
        Commit commit = resourceContent.getCommit();
        verify(feignRemoteForBitbucketBulkActions, times(1)).updateContent("CI-Repo", "jenkin", DirectCommit.from(directCommit()), "Token");
        User author = commit.getAuthor();
        assertThat(commit.getId()).isEqualTo("1234");
        assertThat(author.getLogin()).isEqualTo("sekhar");
        assertThat(author.getEmail()).isEqualTo("some.mail@gmail.com");
        assertThat(resourceContent.getContent()).isNotNull();
    }

    @Test
    void should_create_pull_request_and_return_post_push_commit() throws RemoteSourceControlAuthorizationException {
        com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate newPr = pullRequestToCreate();


        when(feignRemoteForBitbucketBulkActions.createPullRequest("CI-Repo", com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequestToCreate.from(newPr, "CI-Repo", new Project("CI-Project")), "Token"))
                .thenReturn(pullRequest());
        var resourceContent = remoteForBitbucketBulkActionsWrapper.createPullRequest("CI-Repo", newPr, "Token");
        assertThat(resourceContent.getNumber()).isEqualTo(1);
        assertThat(resourceContent.getBaseBranchName()).isEqualTo("dis_id_toRef");
        assertThat(resourceContent.getBranchName()).isEqualTo("dis_id_fromRef");
        assertThat(resourceContent.getCreationDate()).isEqualTo(LocalDateTime.of(2022, 10, 15, 10, 41, 39));
        assertThat(resourceContent.getHtmlUrl()).isEqualTo("href");
        assertThat(resourceContent.getUrl()).isEqualTo("href");
        assertThat(resourceContent.getBranchStartedFromCommit()).isEqualTo("latest_commit");
        assertThat(resourceContent.getRepo().getFullName()).isEqualTo("ci-droid-consumer");
        assertThat(resourceContent.getUser().getEmail()).isEqualTo("some.mail@gmail.com");
    }

    @Test
    void should_fetch_head_reference_for_a_branch() {
        when(feignRemoteForBitbucketBulkActions.fetchHeadReferenceFrom("CI-Repo", "master")).thenReturn(reference());
        var reference = remoteForBitbucketBulkActionsWrapper.fetchHeadReferenceFrom("CI-Repo", "master");

        verify(feignRemoteForBitbucketBulkActions, times(1)).fetchHeadReferenceFrom("CI-Repo", "master");
        assertThat(reference.getRef()).isEqualTo("/master");
        assertThat(reference.getObject().getSha()).isEqualTo("sha key");
    }

    @Test
    void createBranch_from_reference() throws RemoteSourceControlAuthorizationException, BranchAlreadyExistsException {
        when(feignRemoteForBitbucketBulkActions.createBranch("CI-Repo", "master", "reference", "token")).thenReturn(reference());
        var reference = remoteForBitbucketBulkActionsWrapper.createBranch("CI-Repo", "master", "reference", "token");
        verify(feignRemoteForBitbucketBulkActions, times(1)).createBranch("CI-Repo", "master", "reference", "token");
        assertThat(reference.getRef()).isEqualTo("/master");
        assertThat(reference.getObject().getSha()).isEqualTo("sha key");
    }

    @Test
    void should_fetch_repository_when_repository_exists() {
        when(feignRemoteForBitbucketBulkActions.fetchRepository("CI-Repo")).thenReturn(Optional.of(repository()));
        var repository = remoteForBitbucketBulkActionsWrapper.fetchRepository("CI-Repo");
        verify(feignRemoteForBitbucketBulkActions, times(1)).fetchRepository("CI-Repo");
        assertThat(repository.get().getDefaultBranch()).isEqualTo("master");
    }

    @Test
    void should_return_empty_when_repository_dosenot_exists() {
        when(feignRemoteForBitbucketBulkActions.fetchRepository("CI-Repo")).thenReturn(Optional.empty());
        var repository = remoteForBitbucketBulkActionsWrapper.fetchRepository("CI-Repo");
        verify(feignRemoteForBitbucketBulkActions, times(1)).fetchRepository("CI-Repo");
        assertThat(repository).isEmpty();
    }
}
