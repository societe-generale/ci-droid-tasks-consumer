package com.societegenerale.cidroid.tasks.consumer.services;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.DirectPushGitHubInteraction;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.extensions.actionToReplicate.DeleteResourceAction;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.TestAppender;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_AUTHENTICATION_ISSUE;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_CANT_PROVIDE_CONTENT_ISSUE;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_FILE_CONTENT_IS_SAME;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_FILE_DOESNT_EXIST;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_REPO_DOESNT_EXIST;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_UNEXPECTED_EXCEPTION_DURING_PROCESSING;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_OK;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_OK_BUT_PR_CREATION_KO;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_OK_WITH_PR_ALREADY_EXISTING;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_OK_WITH_PR_CREATED;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.BULK_ACTION_COMMIT_PERFORMED;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.BULK_ACTION_PROCESSED;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.BULK_ACTION_PR_CREATED;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActionToPerformServiceTest {

    private static final String MODIFIED_CONTENT = "modifiedContent";

    private static final String REPO_FULL_NAME = "repoFullName";

    private static final String REFS_HEADS = "refs/heads/";

    private final String SOME_USER_NAME = "someUserName";

    private final String SOME_API_ACCESS_TOKEN = "abcdefghij12345";

    private final String SOME_EMAIL = "someEmail@someDomain.com";

    private final String SOME_COMMIT_MESSAGE = "this is the original commit message by the user";

    private final String MASTER_BRANCH = "masterBranch";

    private final String sha1ForHeadOnMaster = "123456abcdef";

    private final String branchNameToCreateForPR = "myPrBranch";

    private final User SOME_USER=new User(SOME_USER_NAME, SOME_EMAIL);

    private final TestAppender testAppender = new TestAppender();


    private final SourceControlBulkActionsPerformer mockRemoteSourceControl = mock(SourceControlBulkActionsPerformer.class);

    private final ActionNotificationService mockActionNotificationService = mock(ActionNotificationService.class);

    private final ResourceContent sampleResourceContentBeforeUpdate = ResourceContent.builder().build();

    private final PullRequest samplePullRequest = new PullRequest(789);

    private final Repository sampleRepository=Repository.builder().build();


    private final ArgumentCaptor<UpdatedResource> updatedResourceCaptor = ArgumentCaptor.forClass(UpdatedResource.class);

    private final ArgumentCaptor<DirectCommit> directCommitCaptor = ArgumentCaptor.forClass(DirectCommit.class);

    private final ArgumentCaptor<PullRequestToCreate> newPrCaptor = ArgumentCaptor.forClass(PullRequestToCreate.class);

    private final ActionToPerformService actionToPerformService = new ActionToPerformService(mockRemoteSourceControl, mockActionNotificationService);

    private final TestActionToPerform testActionToPerform = new TestActionToPerform();

    private final ResourceToUpdate resourceToUpdate = new ResourceToUpdate(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH, StringUtils.EMPTY);

    private UpdatedResource updatedResource;

    private BulkActionToPerform.BulkActionToPerformBuilder bulkActionToPerformBuilder;

    @BeforeEach
    public void setUp() throws RemoteSourceControlAuthorizationException {

        testActionToPerform.setContentToProvide(MODIFIED_CONTENT);
        testActionToPerform.setContinueIfResourceDoesntExist(true);

        bulkActionToPerformBuilder = BulkActionToPerform.builder()
                .userRequestingAction(SOME_USER)
                .sourceControlPersonalToken(SOME_API_ACCESS_TOKEN)
                .gitLogin("some.user.login")
                .email(SOME_EMAIL)
                .commitMessage(SOME_COMMIT_MESSAGE)
                .resourcesToUpdate(List.of(resourceToUpdate))
                .actionToReplicate(testActionToPerform);

        UpdatedResource.Content content = new UpdatedResource.Content();
        content.setHtmlUrl("http://github.com/someRepo/linkToTheResource");

        updatedResource = UpdatedResource.builder().content(content)
                .commit(mock(Commit.class))
                .build();

        when(mockRemoteSourceControl.updateContent(anyString(), anyString(), any(DirectCommit.class), anyString()))
                .thenReturn(updatedResource); // lenient mocking - we're asserting in verify.

        when(mockRemoteSourceControl.fetchCurrentUser(eq(SOME_API_ACCESS_TOKEN),anyString(), anyString()))
            .thenReturn(SOME_USER);


        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger log = logCtx.getLogger("Main");
        log.addAppender(testAppender);

        sampleResourceContentBeforeUpdate.setHtmlLink("http://github.com/someRepo/linkToTheResource");
        sampleResourceContentBeforeUpdate.setSha("someShaFromSetup");
        sampleResourceContentBeforeUpdate.setBase64EncodedContent(GitHubContentBase64codec.encode("some content"));

        samplePullRequest.setHtmlUrl("http://linkToThePr");

        sampleRepository.setDefaultBranch(MASTER_BRANCH);
        sampleRepository.setFullName(REPO_FULL_NAME);
    }

    @AfterEach
    public void after() {
        assertAtLeastOneMonitoringEventOfType(BULK_ACTION_PROCESSED);
        testAppender.events.clear();
    }

    @Test
    void performActionOfType_DIRECT_PUSH() throws RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(sampleResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(MASTER_BRANCH);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK);
    }

    @Test
    void isPassingResourceToUpdateToTheAction_toGenerateItsContent() {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(sampleResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertThat(testActionToPerform.getUsedResourceToUpdate()).isEqualTo(resourceToUpdate);
    }


    @Test
    void dontPushWhenContentIsNotModifiedByAction_andActionIs_DIRECT_PUSH() throws RemoteSourceControlAuthorizationException {

        String contentThatWillNotBeModified = "some content that will not change";
        testActionToPerform.setContentToProvide(contentThatWillNotBeModified);

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        ResourceContent sampleResourceContentWithSpecificContentBeforeUpdate = buildSampleResourceWithContent(contentThatWillNotBeModified);


        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(sampleResourceContentWithSpecificContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_CONTENT_IS_SAME);

    }

    private ResourceContent buildSampleResourceWithContent(String contentThatWillNotBeModified) {
        var sampleResource = ResourceContent.builder()
                .htmlLink("http://github.com/someRepo/linkToTheResource")
                .base64EncodedContent(GitHubContentBase64codec.encode(contentThatWillNotBeModified))
                .sha("someSha")
                .build();

        return sampleResource;
    }

    @Test
    void dontPushWhenContentIsNotModifiedByAction_andActionIs_PULL_REQUEST()
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        String contentThatWillNotBeModified = "some content that will not change";

        testActionToPerform.setContentToProvide(contentThatWillNotBeModified);

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        ResourceContent sampleResourceContentWithSpecificContentBeforeUpdate = buildSampleResourceWithContent(contentThatWillNotBeModified);

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", branchNameToCreateForPR))
                .thenReturn(sampleResourceContentWithSpecificContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_CONTENT_IS_SAME);

    }

    @Test
    void performActionOfType_PULL_REQUEST() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteSourceControl.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(samplePullRequest);

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", branchNameToCreateForPR)).thenReturn(sampleResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(branchNameToCreateForPR);

        verify(mockRemoteSourceControl, times(1)).createPullRequest(eq(REPO_FULL_NAME), newPrCaptor.capture(), anyString());

        assertPullRequestHasBeenCreated(bulkActionToPerform.getCommitMessage());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK_WITH_PR_CREATED);

    }

    @Test
    void continueIfResourceDoesntExist_whenActionPermitsIt() throws RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        ResourceContent nonExistingResourceContent = ResourceContent.builder().build();

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(nonExistingResourceContent);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(MASTER_BRANCH);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK);
    }

    @Test
    void continueIfExistingResourceIsNull_whenActionPermitsIt() throws RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(null);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(MASTER_BRANCH);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK);
    }

    @Test
    void dontDoAnythingIfResourceDoesntExist_whenActionDoesntAllowIt_for_DIRECT_PUSH() throws RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        testActionToPerform.setContinueIfResourceDoesntExist(false);

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(null);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_DOESNT_EXIST);
    }

    @Test
    void dontDoAnythingIfResourceDoesntExist_whenActionDoesntAllowIt_for_PULL_REQUEST()
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();
        testActionToPerform.setContinueIfResourceDoesntExist(false);

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(null);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_DOESNT_EXIST);

    }

    @Test
    void dontDoAnythingIfResourceDoesntExist_whenActionIsDeleteResource()
            throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();
        bulkActionToPerform.setActionToReplicate(new DeleteResourceAction());

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(null);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, never()).updateContent(anyString(), anyString(), any(DirectCommit.class), anyString());
        verify(mockRemoteSourceControl, never()).deleteContent(anyString(), anyString(), any(DirectCommit.class), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_FILE_DOESNT_EXIST);

    }

    @Test
    void dontDoAnythingIfProblemWhileComputingContent() {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        testActionToPerform.setThrowIssueProvidingContentException(true);

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(sampleResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_CANT_PROVIDE_CONTENT_ISSUE);

    }

    @Test
    void deleteResourceWhenRequested() throws RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction())
                                                                            .actionToReplicate(new DeleteResourceAction())
                                                                            .resourcesToUpdate(List.of(resourceToUpdate))
                                                                            .sourceControlPersonalToken(SOME_API_ACCESS_TOKEN)
                                                                            .build();

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(sampleResourceContentBeforeUpdate);

        when(mockRemoteSourceControl.deleteContent(eq(REPO_FULL_NAME), eq("someFile.txt"),any(DirectCommit.class),eq(SOME_API_ACCESS_TOKEN))).thenReturn(updatedResource);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, times(1)).deleteContent(eq(REPO_FULL_NAME),
                eq(resourceToUpdate.getFilePathOnRepo()),
                directCommitCaptor.capture(),
                eq(SOME_API_ACCESS_TOKEN));

        DirectCommit actualCommit = directCommitCaptor.getValue();

        assertThat(actualCommit.getBranch()).isEqualTo(MASTER_BRANCH);
        assertThat(actualCommit.getCommitMessage()).isEqualTo(SOME_COMMIT_MESSAGE + " performed on behalf of someUserName by CI-droid");
        assertThat(actualCommit.getCommitter().getEmail()).isEqualTo(SOME_EMAIL);
        assertThat(actualCommit.getCommitter().getName()).isEqualTo(SOME_USER_NAME);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().hasBeenUpdated()).isTrue();
    }

    @Test
    void reuseBranchIfAlreadyExistWhenDoing_PULL_REQUEST() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteSourceControl.fetchHeadReferenceFrom(REPO_FULL_NAME, branchNameToCreateForPR)).thenReturn(new Reference(
                REFS_HEADS + branchNameToCreateForPR, mock(Reference.ObjectReference.class)));

        when(mockRemoteSourceControl.createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnMaster, SOME_API_ACCESS_TOKEN))
                .thenThrow(new BranchAlreadyExistsException("branch already exists"));

        when(mockRemoteSourceControl.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(samplePullRequest);

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", branchNameToCreateForPR)).thenReturn(sampleResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        assertContentHasBeenUpdatedOnBranch(branchNameToCreateForPR);

        verify(mockRemoteSourceControl, times(1)).createPullRequest(eq(REPO_FULL_NAME), newPrCaptor.capture(), anyString());

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK_WITH_PR_CREATED);

        assertPullRequestHasBeenCreated(bulkActionToPerform.getCommitMessage());

    }

    @Test
    void notifyProperlyWhenIncorrectCredentials_whenCommitting() throws RemoteSourceControlAuthorizationException {

        when(mockRemoteSourceControl.updateContent(eq(REPO_FULL_NAME), anyString(), any(DirectCommit.class), anyString()))
                .thenThrow(new RemoteSourceControlAuthorizationException("invalid credentials"));

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new DirectPushGitHubInteraction()).build();

        when(mockRemoteSourceControl.fetchContent(REPO_FULL_NAME, "someFile.txt", MASTER_BRANCH)).thenReturn(sampleResourceContentBeforeUpdate);

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_AUTHENTICATION_ISSUE);
    }


    @Test
    void notifyProperlyWhenIncorrectCredentials_whenCreatingBranch() throws RemoteSourceControlAuthorizationException, BranchAlreadyExistsException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteSourceControl.createBranch(eq(REPO_FULL_NAME), anyString(), anyString(), anyString()))
                .thenThrow(new RemoteSourceControlAuthorizationException("invalid credentials"));

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_AUTHENTICATION_ISSUE);
    }

    @Test
    void notifyProperlyWhenUnexpectedError() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteSourceControl.createBranch(eq(REPO_FULL_NAME), anyString(), anyString(), anyString()))
                .thenThrow(new NullPointerException("some dummy NPE"));

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_UNEXPECTED_EXCEPTION_DURING_PROCESSING);
    }

    @Test
    void notifyProperlyWhenRepoDoesntExist() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        when(mockRemoteSourceControl.fetchRepository(REPO_FULL_NAME))
                .thenReturn(Optional.empty());

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_KO_REPO_DOESNT_EXIST);

        verify(mockRemoteSourceControl, never()).fetchHeadReferenceFrom(any(), any());
    }

    @Test
    void shouldCreatePRwithProvidedTitle() throws RemoteSourceControlAuthorizationException, BranchAlreadyExistsException {

        mockPullRequestSpecificBehavior();
        when(mockRemoteSourceControl.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(samplePullRequest);

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR, "new feature branch")).build();

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, times(1)).createPullRequest(eq(REPO_FULL_NAME),
                newPrCaptor.capture(),
                eq(SOME_API_ACCESS_TOKEN));

        assertThat(newPrCaptor.getValue().getTitle()).isEqualTo("new feature branch");
    }

    @Test
    void shouldCreatePRwithBranchName_whenPRtitleIsNotProvided() throws RemoteSourceControlAuthorizationException, BranchAlreadyExistsException {

        mockPullRequestSpecificBehavior();
        when(mockRemoteSourceControl.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class), anyString())).thenReturn(samplePullRequest);

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR, null)).build();

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, times(1)).createPullRequest(eq(REPO_FULL_NAME),
                newPrCaptor.capture(),
                eq(SOME_API_ACCESS_TOKEN));

        assertThat(newPrCaptor.getValue().getTitle()).isEqualTo(branchNameToCreateForPR);

    }

    @Test
    void shouldCreatePRbranchFromProvidedBranch() throws RemoteSourceControlAuthorizationException, BranchAlreadyExistsException {

        BulkActionToPerform bulkActionToPerform = doApullRequestAction();

        bulkActionToPerform.setResourcesToUpdate(singletonList(new ResourceToUpdate(REPO_FULL_NAME, "someFile.txt", "someBranch", StringUtils.EMPTY)));

        String sha1ForHeadOnSomeBranch = "987456poiuytrewq";

        String sourceBranchForPr = "someBranch";

        Reference dummyHeadOnSomeBranchReference = new Reference(
                REFS_HEADS + sourceBranchForPr, new Reference.ObjectReference("commit", sha1ForHeadOnSomeBranch));

        when(mockRemoteSourceControl.fetchHeadReferenceFrom(REPO_FULL_NAME, sourceBranchForPr)).thenReturn(dummyHeadOnSomeBranchReference);

        when(mockRemoteSourceControl.createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnSomeBranch, SOME_API_ACCESS_TOKEN))
                .thenReturn(new Reference(REFS_HEADS + branchNameToCreateForPR, mock(Reference.ObjectReference.class)));

        when(mockRemoteSourceControl.createPullRequest(eq(REPO_FULL_NAME), newPrCaptor.capture(), anyString())).thenReturn(samplePullRequest);


        actionToPerformService.perform(bulkActionToPerform);

        verify(mockRemoteSourceControl, times(1)).createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnSomeBranch, SOME_API_ACCESS_TOKEN);

        PullRequestToCreate pr = newPrCaptor.getValue();
        assertThat(pr.getHead()).isEqualTo(branchNameToCreateForPR);
        assertThat(pr.getBase()).isEqualTo(sourceBranchForPr);

    }

    @Test
    void dontCreatePR_ifAlreadyAnOpenPRonSameBranch() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        mockPullRequestSpecificBehavior();

        int prNumber = 3;

        PullRequest openPRonBranch1 = new PullRequest(prNumber);
        openPRonBranch1.setBranchName(branchNameToCreateForPR);

        when(mockRemoteSourceControl.fetchOpenPullRequests(REPO_FULL_NAME)).thenReturn(singletonList(openPRonBranch1));

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR, null)).build();

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK_WITH_PR_ALREADY_EXISTING);
    }


    @Test
    void notifyProperlyWhenIssueWithPRcreation() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        mockPullRequestSpecificBehavior();

        //assuming there's an unexpected exception while trying to create the PR..
        when(mockRemoteSourceControl.createPullRequest(eq(REPO_FULL_NAME), any(PullRequestToCreate.class),anyString())).thenThrow(new RuntimeException("some unexpected exception... "));

        BulkActionToPerform bulkActionToPerform = bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR, null)).build();

        actionToPerformService.perform(bulkActionToPerform);

        verify(mockActionNotificationService, times(1)).handleNotificationsFor(eq(bulkActionToPerform),
                eq(resourceToUpdate),
                updatedResourceCaptor.capture());

        assertThat(updatedResourceCaptor.getValue().getUpdateStatus()).isEqualTo(UPDATE_OK_BUT_PR_CREATION_KO);
    }

    private BulkActionToPerform doApullRequestAction() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

        mockPullRequestSpecificBehavior();

        return bulkActionToPerformBuilder.gitHubInteraction(new PullRequestGitHubInteraction(branchNameToCreateForPR, null)).build();
    }

    private void assertPullRequestHasBeenCreated(String expectedCommitMessage) {
        PullRequestToCreate actualPrToCreate = newPrCaptor.getValue();
        assertThat(actualPrToCreate.getBase()).isEqualTo(MASTER_BRANCH);
        assertThat(actualPrToCreate.getHead()).isEqualTo(branchNameToCreateForPR);
        assertThat(actualPrToCreate.getTitle()).isEqualTo(branchNameToCreateForPR);

        assertThat(actualPrToCreate.getBody()).startsWith("performed on behalf of someUserName by CI-droid");
        assertThat(actualPrToCreate.getBody()).endsWith(expectedCommitMessage);

        assertAtLeastOneMonitoringEventOfType(BULK_ACTION_PR_CREATED);

    }

    private void assertContentHasBeenUpdatedOnBranch(String branchName) throws RemoteSourceControlAuthorizationException {

        verify(mockRemoteSourceControl, times(1)).updateContent(eq(REPO_FULL_NAME),
                eq("someFile.txt"),
                directCommitCaptor.capture(),
                eq(SOME_API_ACCESS_TOKEN));

        DirectCommit actualCommit = directCommitCaptor.getValue();

        assertThat(actualCommit.getBranch()).isEqualTo(branchName);
        assertThat(actualCommit.getCommitMessage()).isEqualTo(SOME_COMMIT_MESSAGE + " performed on behalf of someUserName by CI-droid");
        assertThat(actualCommit.getCommitter().getEmail()).isEqualTo(SOME_EMAIL);
        assertThat(actualCommit.getCommitter().getName()).isEqualTo(SOME_USER_NAME);

        String expectedEncodedContent = new String(Base64.getEncoder().encode(MODIFIED_CONTENT.getBytes()));
        assertThat(actualCommit.getBase64EncodedContent()).isEqualTo(expectedEncodedContent);

        assertAtLeastOneMonitoringEventOfType(BULK_ACTION_COMMIT_PERFORMED);
    }

    private void assertAtLeastOneMonitoringEventOfType(String eventType) {

        assertThat(testAppender.events.stream()
                .filter(logEvent -> logEvent.getMDCPropertyMap().getOrDefault("metricName", "NOT_FOUND").equals(eventType)).findAny())
                .isPresent();
    }

    private void mockPullRequestSpecificBehavior() throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {
        when(mockRemoteSourceControl.fetchRepository(REPO_FULL_NAME)).thenReturn(Optional.of(sampleRepository));

        Reference dummyHeadOnMasterReference = new Reference(REFS_HEADS + MASTER_BRANCH, new Reference.ObjectReference("commit", sha1ForHeadOnMaster));
        when(mockRemoteSourceControl.fetchHeadReferenceFrom(REPO_FULL_NAME, MASTER_BRANCH)).thenReturn(dummyHeadOnMasterReference);

        when(mockRemoteSourceControl.createBranch(REPO_FULL_NAME, branchNameToCreateForPR, sha1ForHeadOnMaster, SOME_API_ACCESS_TOKEN))
                .thenReturn(new Reference(REFS_HEADS + branchNameToCreateForPR, mock(Reference.ObjectReference.class)));
    }

}
