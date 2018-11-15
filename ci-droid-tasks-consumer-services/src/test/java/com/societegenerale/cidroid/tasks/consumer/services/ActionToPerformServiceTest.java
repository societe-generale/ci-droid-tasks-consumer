package com.societegenerale.cidroid.tasks.consumer.services;

import com.oneeyedmen.fakir.Faker;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.DirectPushGitHubInteraction;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.GitHubAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Base64;

import static com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource.UpdateStatus.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class ActionToPerformServiceTest {

    private static final String MODIFIED_CONTENT = "modifiedContent";

    private static final String REPO_FULL_NAME = "repoFullName";

    public static final String REFS_HEADS = "refs/heads/";

    private final String SOME_USER_NAME = "someUserName";

    private final String SOME_OAUTH_TOKEN = "abcdefghij12345";

    private final String SOME_EMAIL = "someEmail@someDomain.com";

    private final String SOME_COMMIT_MESSAGE = "this is the original commit message by the user";

    private final String MASTER_BRANCH="masterBranch";

    private String sha1ForHeadOnMaster = "123456abcdef";

    private String branchNameToCreateForPR = "myPrBranch";


    private RemoteGitHub mockRemoteGitHub = mock(RemoteGitHub.class);

    private ActionNotificationService mockActionNotificationService = mock(ActionNotificationService.class);

    ResourceContent fakeResourceContentBeforeUpdate = new Faker<ResourceContent>() {
        String htmlLink = "http://github.com/someRepo/linkToTheResource";
    }.get();

    PullRequest fakePullRequest = new Faker<PullRequest>() {
        int number = 789;

        String htmlUrl = "http://linkToThePr";
    }.get();

    Repository fakeRepository = new Faker<Repository>() {
        String defaultBranch = MASTER_BRANCH;

        String fullName = REPO_FULL_NAME;
    }.get();

    private ArgumentCaptor<UpdatedResource> updatedResourceCaptor = ArgumentCaptor.forClass(UpdatedResource.class);

    private ArgumentCaptor<DirectCommit> directCommitCaptor = ArgumentCaptor.forClass(DirectCommit.class);

    private ArgumentCaptor<PullRequestToCreate> newPrCaptor = ArgumentCaptor.forClass(PullRequestToCreate.class);

    private ActionToPerformService actionToPerformService = new ActionToPerformService(mockRemoteGitHub, mockActionNotificationService);

    private TestActionToPerform testActionToPerform = new TestActionToPerform();

    private ResourceToUpdate resourceToUpdate = new ResourceToUpdate(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH, StringUtils.EMPTY);

    private UpdatedResource updatedResource;

    private BulkActionToPerform.BulkActionToPerformBuilder bulkActionToPerformBuilder;

    @Before
    public void setup() throws GitHubAuthorizationException {

        testActionToPerform.setContentToProvide(MODIFIED_CONTENT);
        testActionToPerform.setContinueIfResourceDoesntExist(true);

        bulkActionToPerformBuilder = BulkActionToPerform.builder().gitLogin(SOME_USER_NAME)
                .gitHubOauthToken(SOME_OAUTH_TOKEN)
                .email(SOME_EMAIL)
                .commitMessage(SOME_COMMIT_MESSAGE)
                .resourcesToUpdate(Arrays.asList(resourceToUpdate))
                .actionToReplicate(testActionToPerform);

        UpdatedResource.Content content = new UpdatedResource.Content();
        content.setHtmlUrl("http://github.com/someRepo/linkToTheResource");

        updatedResource = UpdatedResource.builder().content(content)
                .commit(Faker.fakeA(Commit.class))
                .build();

        when(mockRemoteGitHub.updateContent(anyString(), anyString(), any(DirectCommit.class), anyString()))
                .thenReturn(updatedResource); // lenient mocking - we're asserting in verify.
    }

    @Test
    public void performActionOfType_DIRECT_PUSH() throws GitHubAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(fakeResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(MASTER_BRANCH);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK);
    }

    @Test
    public void isPassingResourceToUpdateToTheAction_toGenerateItsContent()  {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(fakeResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertThat(testActionToPerform.getUsedResourceToUpdate()).isEqualTo(resourceToUpdate);
    }


    @Test
    public void dontPushWhenContentIsNotModifiedByAction_andActionIs_DIRECT_PUSH() throws GitHubAuthorizationException {

        String contentThatWillNotBeModified = "some content that will not change";
        testActionToPerform.setContentToProvide(contentThatWillNotBeModified);

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        ResourceContent fakeResourceContentWithSpecificContentBeforeUpdate = new Faker<ResourceContent>() {
            String htmlLink = "http://github.com/someRepo/linkToTheResource";

            String base64EncodedContent = GitHubContentBase64codec.encode(contentThatWillNotBeModified);
        }.get();

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(fakeResourceContentWithSpecificContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteGitHub, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_CONTENT_IS_SAME);

    }

    @Test
    public void dontPushWhenContentIsNotModifiedByAction_andActionIs_PULL_REQUEST()
            throws BranchAlreadyExistsException, GitHubAuthorizationException {

        String contentThatWillNotBeModified = "some content that will not change";

        testActionToPerform.setContentToProvide(contentThatWillNotBeModified);

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        ResourceContent fakeResourceContentWithSpecificContentBeforeUpdate = new Faker<ResourceContent>() {
            String htmlLink = "http://github.com/someRepo/linkToTheResource";

            String base64EncodedContent = GitHubContentBase64codec.encode(contentThatWillNotBeModified);
        }.get();

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", branchNameToCreateForPR))
                .thenReturn(fakeResourceContentWithSpecificContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteGitHub, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_CONTENT_IS_SAME);

    }

    @Test
    public void performActionOfType_PULL_REQUEST() throws BranchAlreadyExistsException, GitHubAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteGitHub.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(fakePullRequest);

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", branchNameToCreateForPR)).thenReturn(fakeResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(branchNameToCreateForPR);

        verify(mockRemoteGitHub, times(1)).createPullRequest(eq(REPO_FULL_NAME), newPrCaptor.capture(),anyString());

        assertPullRequestHasBeenCreated(bulkActionToPerform.getCommitMessage());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK_WITH_PR_CREATED);

    }

    @Test
    public void continueIfResourceDoesntExist_whenActionPermitsIt() throws GitHubAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        ResourceContent nonExistingResourceContent = new ResourceContent();

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(nonExistingResourceContent);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(MASTER_BRANCH);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK);
    }

    @Test
    public void continueIfExistingResourceIsNull_whenActionPermitsIt() throws GitHubAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(null);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(MASTER_BRANCH);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK);
    }

    @Test
    public void dontDoAnythingIfResourceDoesntExist_whenActionDoesntAllowIt_for_DIRECT_PUSH() throws GitHubAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        testActionToPerform.setContinueIfResourceDoesntExist(false);

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(null);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteGitHub, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_DOESNT_EXIST);
    }

    @Test
    public void dontDoAnythingIfResourceDoesntExist_whenActionDoesntAllowIt_for_PULL_REQUEST()
            throws BranchAlreadyExistsException, GitHubAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();
        testActionToPerform.setContinueIfResourceDoesntExist(false);

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(null);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteGitHub, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_DOESNT_EXIST);

    }

    @Test
    public void dontDoAnythingIfProblemWhileComputingContent() {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        testActionToPerform.setThrowIssueProvidingContentException(true);

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(fakeResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_CANT_PROVIDE_CONTENT_ISSUE);

    }

    @Test
    public void reuseBranchIfAlreadyExistWhenDoing_PULL_REQUEST() throws BranchAlreadyExistsException, GitHubAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteGitHub.fetchHeadReferenceFrom(REPO_FULL_NAME, branchNameToCreateForPR)).thenReturn(new Reference(
                REFS_HEADS +branchNameToCreateForPR,Faker.fakeA(Reference.ObjectReference.class)));

        when(mockRemoteGitHub.createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnMaster, SOME_OAUTH_TOKEN))
                .thenThrow(new BranchAlreadyExistsException("branch already exists"));

        when(mockRemoteGitHub.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(fakePullRequest);

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", branchNameToCreateForPR)).thenReturn(fakeResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(branchNameToCreateForPR);

        verify(mockRemoteGitHub, times(1)).createPullRequest(eq(REPO_FULL_NAME), newPrCaptor.capture(), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK_WITH_PR_CREATED);

        assertPullRequestHasBeenCreated(bulkActionToPerform.getCommitMessage());

    }

    @Test
    public void notifyProperlyWhenIncorrectCredentials_whenCommitting() throws GitHubAuthorizationException {

        when(mockRemoteGitHub.updateContent(eq(REPO_FULL_NAME), anyString(), any(DirectCommit.class), anyString()))
                .thenThrow(new GitHubAuthorizationException("invalid credentials"));

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteGitHub.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(fakeResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_AUTHENTICATION_ISSUE);
    }


    @Test
    public void notifyProperlyWhenIncorrectCredentials_whenCreatingBranch() throws GitHubAuthorizationException, BranchAlreadyExistsException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteGitHub.createBranch(eq(REPO_FULL_NAME), anyString(), anyString(), anyString()))
                .thenThrow(new GitHubAuthorizationException("invalid credentials"));

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_AUTHENTICATION_ISSUE);
    }

    @Test
    public void shouldCreatePRwithProvidedTitle() throws GitHubAuthorizationException, BranchAlreadyExistsException {

        mockPullRequestSpecificBehavior();
        when(mockRemoteGitHub.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(fakePullRequest);

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR,"new feature branch")).build();

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteGitHub, times(1)).createPullRequest(eq(REPO_FULL_NAME),
                newPrCaptor.capture(),
                eq(SOME_OAUTH_TOKEN));

        assertThat(newPrCaptor.getValue().getTitle()).isEqualTo("new feature branch");
    }

    @Test
    public void shouldCreatePRwithBranchName_whenPRtitleIsNotProvided() throws GitHubAuthorizationException, BranchAlreadyExistsException {

        mockPullRequestSpecificBehavior();
        when(mockRemoteGitHub.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(fakePullRequest);

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR,null)).build();

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteGitHub, times(1)).createPullRequest(eq(REPO_FULL_NAME),
                newPrCaptor.capture(),
                eq(SOME_OAUTH_TOKEN));

        assertThat(newPrCaptor.getValue().getTitle()).isEqualTo(branchNameToCreateForPR);

    }

    @Test
    public void shouldCreatePRbranchFromProvidedBranch() throws GitHubAuthorizationException, BranchAlreadyExistsException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        bulkActionToPerform.setResourcesToUpdate(Arrays.asList( new ResourceToUpdate(REPO_FULL_NAME, "someFile.txt", "someBranch", StringUtils.EMPTY)));

        String sha1ForHeadOnSomeBranch = "987456poiuytrewq";

        String sourceBranchForPr="someBranch";

        Reference dummyHeadOnSomeBranchReference=new Reference(
                REFS_HEADS +sourceBranchForPr,new Reference.ObjectReference("commit", sha1ForHeadOnSomeBranch));

        when(mockRemoteGitHub.fetchHeadReferenceFrom(REPO_FULL_NAME, sourceBranchForPr)).thenReturn(dummyHeadOnSomeBranchReference);

        when(mockRemoteGitHub.createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnSomeBranch, SOME_OAUTH_TOKEN))
                .thenReturn(new Reference(REFS_HEADS +branchNameToCreateForPR,Faker.fakeA(Reference.ObjectReference.class)));

        when(mockRemoteGitHub.createPullRequest(eq(REPO_FULL_NAME), newPrCaptor.capture(), anyString())).thenReturn(fakePullRequest);


        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteGitHub,times(1)).createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnSomeBranch, SOME_OAUTH_TOKEN);

        PullRequestToCreate pr=newPrCaptor.getValue();
        assertThat(pr.getHead()).isEqualTo(branchNameToCreateForPR);
        assertThat(pr.getBase()).isEqualTo(sourceBranchForPr);

    }

    @Test
    public void dontCreatePR_ifAlreadyAnOpenPRonSameBranch() throws BranchAlreadyExistsException, GitHubAuthorizationException {

        mockPullRequestSpecificBehavior();

        int prNumber=3;

        PullRequest openPRonBranch1=new PullRequest(prNumber);
        openPRonBranch1.setBranchName(branchNameToCreateForPR);

        when(mockRemoteGitHub.fetchOpenPullRequests(eq(REPO_FULL_NAME))).thenReturn(Arrays.asList(openPRonBranch1));

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR,null)).build();

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK_WITH_PR_ALREADY_EXISTING);
    }

    private BulkActionToPerform doApullRequestAction() throws BranchAlreadyExistsException, GitHubAuthorizationException {

        mockPullRequestSpecificBehavior();

        return bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR,null)).build();
    }

    private void assertPullRequestHasBeenCreated(String expectedCommitMessage) {
        PullRequestToCreate actualPrToCreate = newPrCaptor.getValue();
        assertThat(actualPrToCreate.getBase()).isEqualTo(MASTER_BRANCH);
        assertThat(actualPrToCreate.getHead()).isEqualTo(branchNameToCreateForPR);
        assertThat(actualPrToCreate.getTitle()).isEqualTo(branchNameToCreateForPR);

        assertThat(actualPrToCreate.getBody()).startsWith("performed on behalf of someUserName by CI-droid");
        assertThat(actualPrToCreate.getBody()).endsWith(expectedCommitMessage);
    }

    private void assertContentHasBeenUpdatedOnBranch(String branchName) throws GitHubAuthorizationException {

        verify(mockRemoteGitHub, times(1)).updateContent(eq(REPO_FULL_NAME),
                eq("someFile.txt"),
                directCommitCaptor.capture(),
                eq(SOME_OAUTH_TOKEN));

        DirectCommit actualCommit = directCommitCaptor.getValue();

        assertThat(actualCommit.getBranch()).isEqualTo(branchName);
        assertThat(actualCommit.getCommitMessage()).isEqualTo(SOME_COMMIT_MESSAGE + " performed on behalf of someUserName by CI-droid");
        assertThat(actualCommit.getCommitter().getEmail()).isEqualTo(SOME_EMAIL);
        assertThat(actualCommit.getCommitter().getName()).isEqualTo(SOME_USER_NAME);

        String expectedEncodedContent = new String(Base64.getEncoder().encode(MODIFIED_CONTENT.getBytes()));
        assertThat(actualCommit.getBase64EncodedContent()).isEqualTo(expectedEncodedContent);

    }

    private void mockPullRequestSpecificBehavior() throws BranchAlreadyExistsException, GitHubAuthorizationException {
        when(mockRemoteGitHub.fetchRepository(REPO_FULL_NAME)).thenReturn(fakeRepository);

        Reference dummyHeadOnMasterReference = new Reference(REFS_HEADS +MASTER_BRANCH,new Reference.ObjectReference("commit", sha1ForHeadOnMaster));
        when(mockRemoteGitHub.fetchHeadReferenceFrom(REPO_FULL_NAME, MASTER_BRANCH)).thenReturn(dummyHeadOnMasterReference);

        when(mockRemoteGitHub.createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnMaster, SOME_OAUTH_TOKEN))
                .thenReturn(new Reference(REFS_HEADS +branchNameToCreateForPR,Faker.fakeA(Reference.ObjectReference.class)));
    }

}