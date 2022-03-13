package com.societegenerale.cidroid.tasks.consumer.services;

import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.DURATION;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.PR_NUMBER;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringAttributes.REPO;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.BULK_ACTION_COMMIT_PERFORMED;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.BULK_ACTION_PROCESSED;
import static com.societegenerale.cidroid.tasks.consumer.services.monitoring.MonitoringEvents.BULK_ACTION_PR_CREATED;

import com.societegenerale.cidroid.api.IssueProvidingContentException;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.api.gitHubInteractions.DirectPushGitHubInteraction;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.extensions.actionToReplicate.DeleteResourceAction;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.Event;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

@Slf4j
public class ActionToPerformService {

    private final SourceControlBulkActionsPerformer remoteSourceControl;

    private final ActionNotificationService actionNotificationService;

    public ActionToPerformService(SourceControlBulkActionsPerformer remoteSourceControl, ActionNotificationService actionNotificationService) {
        this.remoteSourceControl = remoteSourceControl;
        this.actionNotificationService = actionNotificationService;
    }

    public void perform(BulkActionToPerform action) {

        StopWatch stopWatchForMonitoring = StopWatch.createStarted();

        var userRequestingAction=remoteSourceControl.fetchCurrentUser(action.getSourceControlPersonalToken());

        var actionWithUser=action.toBuilder().userRequestingAction(userRequestingAction).build();


        //we're supposed to have only one element in list, but this may change in the future : we'll loop over them.
        ResourceToUpdate resourceToUpdate = actionWithUser.getResourcesToUpdate().get(0);

        String repoFullName = resourceToUpdate.getRepoFullName();

        try {

            if (actionWithUser.getGitHubInteraction() instanceof DirectPushGitHubInteraction) {

                UpdatedResource updatedResource = updateRemoteResource(repoFullName, resourceToUpdate, actionWithUser, resourceToUpdate.getBranchName());

                actionNotificationService.handleNotificationsFor(actionWithUser, resourceToUpdate, updatedResource);

            } else if (actionWithUser.getGitHubInteraction() instanceof PullRequestGitHubInteraction) {

                PullRequestGitHubInteraction pullRequestAction = (PullRequestGitHubInteraction) actionWithUser.getGitHubInteraction();

                Optional<Repository> optionalImpactedRepo = remoteSourceControl.fetchRepository(repoFullName);

                //if repo doesn't exist, notify
                if(optionalImpactedRepo.isEmpty()){
                    actionNotificationService.handleNotificationsFor(actionWithUser, resourceToUpdate, UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_REPO_DOESNT_EXIST));
                    return;
                }

                Repository impactedRepo=optionalImpactedRepo.get();

                String branchNameForPR = pullRequestAction.getBranchNameToCreate();

                String branchFromWhichToCreatePrBranch= resourceToUpdate.getBranchName() == null ? impactedRepo.getDefaultBranch() : resourceToUpdate.getBranchName();

                Reference masterCommitReference = remoteSourceControl.fetchHeadReferenceFrom(repoFullName,branchFromWhichToCreatePrBranch );

                Reference branchToUseForPr = null;

                try {
                    branchToUseForPr = remoteSourceControl.createBranch(repoFullName, branchNameForPR, masterCommitReference.getObject().getSha(),
                        actionWithUser.getSourceControlPersonalToken());
                } catch (BranchAlreadyExistsException e) {

                    log.warn("branch " + branchNameForPR + " already exists, reusing it");

                    //TODO maybe we should add field in Reference to identify when it hasn't been created as expected
                    branchToUseForPr = remoteSourceControl.fetchHeadReferenceFrom(repoFullName, branchNameForPR);
                }

                UpdatedResource updatedResource;

                if (branchToUseForPr == null) {
                    //TODO test this scenario
                    updatedResource = UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_BRANCH_CREATION_ISSUE);

                } else {
                    updatedResource = updateRemoteResource(repoFullName, resourceToUpdate, actionWithUser, branchNameForPR);

                    if (updatedResource.hasBeenUpdated()) {

                        //branchFromWhichToCreatePrBranch is also the target for the PR to be merged to
                        createPullRequest(actionWithUser, impactedRepo, branchToUseForPr, branchFromWhichToCreatePrBranch, updatedResource);
                    }
                }

                actionNotificationService.handleNotificationsFor(actionWithUser, resourceToUpdate, updatedResource);
            } else {
                log.warn("unknown gitHub interaction type : {}", actionWithUser.getGitHubInteraction());
            }

        } catch (RemoteSourceControlAuthorizationException e) {
            log.warn("Github authorization problem while processing "+actionWithUser,e);
            actionNotificationService.handleNotificationsFor(actionWithUser, resourceToUpdate, UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_AUTHENTICATION_ISSUE));
        }
        catch (Exception e) {
            log.warn("problem while processing "+actionWithUser,e);
            actionNotificationService.handleNotificationsFor(actionWithUser, resourceToUpdate, UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_UNEXPECTED_EXCEPTION_DURING_PROCESSING));
        }
        finally {
            publishMonitoringEventForBulkActionProcessed(actionWithUser, stopWatchForMonitoring, repoFullName);
        }

    }

    private void createPullRequest(BulkActionToPerform action, Repository impactedRepo, Reference prBranch, String targetBranchForPR, UpdatedResource updatedResource) {

        Optional<PullRequest> existingOpenPRforBranch=findExistingOpenPRforBranch(impactedRepo,prBranch);

        if(existingOpenPRforBranch.isPresent()){
            updatedResource.getContent().setHtmlUrl(existingOpenPRforBranch.get().getHtmlUrl());
            updatedResource.setUpdateStatus(UpdatedResource.UpdateStatus.UPDATE_OK_WITH_PR_ALREADY_EXISTING);
        }
        else{
            Optional<PullRequest> createdPr = createPrOnBranch(impactedRepo, prBranch, targetBranchForPR, action);

            if (createdPr.isPresent()) {
                updatedResource.getContent().setHtmlUrl(createdPr.get().getHtmlUrl());
                updatedResource.setUpdateStatus(UpdatedResource.UpdateStatus.UPDATE_OK_WITH_PR_CREATED);

                publishMonitoringEventForPRcreated(impactedRepo, targetBranchForPR, createdPr);

            } else {
                //TODO test this scenario
                updatedResource.setUpdateStatus(UpdatedResource.UpdateStatus.UPDATE_OK_BUT_PR_CREATION_KO);
            }
        }

    }

    private Optional<PullRequest> findExistingOpenPRforBranch(Repository repo, Reference prBranch) {

        List<PullRequest> openPRs= remoteSourceControl.fetchOpenPullRequests(repo.getFullName());

        return openPRs.stream().filter(pr -> pr.doneOnBranch(prBranch.getBranchName())).findAny();

    }

    private UpdatedResource updateRemoteResource(String repoFullName, ResourceToUpdate resourceToUpdate, BulkActionToPerform action,
            String onBranch) throws RemoteSourceControlAuthorizationException {

        ResourceContent existingResourceContent = remoteSourceControl
                .fetchContent(repoFullName, resourceToUpdate.getFilePathOnRepo(), onBranch);

        if(action.getActionToReplicate() instanceof DeleteResourceAction){
            if (existingResourceExists(existingResourceContent)){
                UpdatedResource deletedResource = deleteResource(action, resourceToUpdate, existingResourceContent, onBranch);

                log.info("{} deleted on repo {}, on branch {}. SHA1: {}", resourceToUpdate.getFilePathOnRepo(), repoFullName, onBranch,
                        deletedResource.getCommit().getId());

                return deletedResource;
            }
        }

        String decodedOriginalContent = null;
        String newContent = null;

        ActionToReplicate actionToReplicate = action.getActionToReplicate();

        try {
            if (existingResourceExists(existingResourceContent)) {
                decodedOriginalContent = GitHubContentBase64codec.decode(existingResourceContent.getBase64EncodedContent());
                newContent = actionToReplicate.provideContent(decodedOriginalContent,resourceToUpdate);
            }
            else if (actionToReplicate.canContinueIfResourceDoesntExist()) {
                newContent = actionToReplicate.provideContent(null);
            }
            else {
                //existing resource doesnt exist and we should not continue

                log.info("{} NOT updated on repo {}, on branch {}, as it doesnt exist", resourceToUpdate.getFilePathOnRepo(),
                        repoFullName, onBranch);

                return UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_FILE_DOESNT_EXIST);

            }
        } catch (IssueProvidingContentException e) {
            log.warn("problem while computing the new content", e);
            return UpdatedResource
                    .notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_CANT_PROVIDE_CONTENT_ISSUE, existingResourceContent.getHtmlLink());
        }

        if (contentIsSame(decodedOriginalContent, newContent)) {
            log.info("{} NOT updated on repo {}, on branch {}, as new content is same as existing content", resourceToUpdate.getFilePathOnRepo(),
                    repoFullName, onBranch);

            return UpdatedResource
                    .notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_FILE_CONTENT_IS_SAME, existingResourceContent.getHtmlLink());
        }

        UpdatedResource updatedResource = commitResource(action, newContent, resourceToUpdate, existingResourceContent, onBranch);

        logWhatHasBeenDone(repoFullName, resourceToUpdate, onBranch, existingResourceContent, decodedOriginalContent, updatedResource);

        return updatedResource;

    }

    private UpdatedResource deleteResource(BulkActionToPerform action, ResourceToUpdate resourceToDelete, ResourceContent existingResourceContent, String onBranch) throws RemoteSourceControlAuthorizationException {

        DirectCommit directCommit = buildDirectCommit(action,existingResourceContent, onBranch);

        UpdatedResource updatedResource = remoteSourceControl
                .deleteContent(resourceToDelete.getRepoFullName(), resourceToDelete.getFilePathOnRepo(), directCommit,
                        action.getSourceControlPersonalToken());

        publishMonitoringEventForCommitPerformed(resourceToDelete, updatedResource);

        updatedResource.setUpdateStatus(UpdatedResource.UpdateStatus.UPDATE_OK);

        return updatedResource;
    }

    private DirectCommit buildDirectCommit(BulkActionToPerform action, ResourceContent existingResourceContent, String onBranch) {

        DirectCommit directCommit = new DirectCommit();

        if (existingResourceExists(existingResourceContent)) {
            directCommit.setPreviousVersionSha1(existingResourceContent.getSha());
        }

        directCommit.setBranch(onBranch);
        directCommit.setCommitter(new DirectCommit.Committer(action.getUserRequestingAction().getLogin(), action.getEmail()));
        directCommit.setCommitMessage(action.getCommitMessage() + " performed on behalf of " + action.getUserRequestingAction().getLogin() + " by CI-droid");

        return directCommit;
    }

    private boolean existingResourceExists(ResourceContent existingResourceContent) {
        return existingResourceContent != null && existingResourceContent.getSha() != null;
    }

    private void logWhatHasBeenDone(String repoFullName, ResourceToUpdate resourceToUpdate, String onBranch, ResourceContent existingResourceContent,
            String decodedOriginalContent, UpdatedResource updatedResource) {

        if (resourceWasNotExisting(decodedOriginalContent)) {

            log.info("{} created on repo {}, on branch {}. SHA1: {}", resourceToUpdate.getFilePathOnRepo(), repoFullName, onBranch,
                    updatedResource.getCommit().getId());
        } else {
            log.info("{} updated on repo {}, on branch {}. old SHA1: {} . new SHA1: {}", resourceToUpdate.getFilePathOnRepo(), repoFullName, onBranch,
                    updatedResource.getCommit().getId(), existingResourceContent.getSha());
        }
    }

    private UpdatedResource commitResource(BulkActionToPerform action, String newContent, ResourceToUpdate resourceToUpdate,
            ResourceContent existingResourceContent, String onBranch) throws RemoteSourceControlAuthorizationException {

        DirectCommit directCommit = buildDirectCommit(action,existingResourceContent, onBranch);

        directCommit.setBase64EncodedContent(GitHubContentBase64codec.encode(newContent));

        UpdatedResource updatedResource = remoteSourceControl
                .updateContent(resourceToUpdate.getRepoFullName(), resourceToUpdate.getFilePathOnRepo(), directCommit,
                        action.getSourceControlPersonalToken());

        publishMonitoringEventForCommitPerformed(resourceToUpdate, updatedResource);

        updatedResource.setUpdateStatus(UpdatedResource.UpdateStatus.UPDATE_OK);

        return updatedResource;
    }

    private void publishMonitoringEventForCommitPerformed(ResourceToUpdate resourceToUpdate, UpdatedResource updatedResource) {
        Event techEvent = Event.technical(BULK_ACTION_COMMIT_PERFORMED);
        techEvent.addAttribute(REPO, resourceToUpdate.getRepoFullName());
        techEvent.addAttribute("resourceName", resourceToUpdate.getFilePathOnRepo());
        techEvent.addAttribute("branchName", resourceToUpdate.getBranchName());
        techEvent.addAttribute("newCommitSha", updatedResource.getCommit().getId());
        techEvent.publish();
    }

    private void publishMonitoringEventForPRcreated(Repository impactedRepo, String targetBranchForPR, Optional<PullRequest> createdPr) {
        Event techEvent = Event.technical(BULK_ACTION_PR_CREATED);
        techEvent.addAttribute(REPO, impactedRepo.getFullName());
        techEvent.addAttribute("targetBranchForPR", targetBranchForPR);
        techEvent.addAttribute(PR_NUMBER, String.valueOf(createdPr.get().getNumber()));
        techEvent.publish();
    }

    public void publishMonitoringEventForBulkActionProcessed(BulkActionToPerform action, StopWatch stopWatchForMonitoring, String repoFullName) {

        stopWatchForMonitoring.stop();

        Event techEvent = Event.technical(BULK_ACTION_PROCESSED);
        techEvent.addAttribute(REPO, repoFullName);
        techEvent.addAttribute("bulkActionReceived", action.toString());
        techEvent.addAttribute("bulkActionType", action.getActionType());
        techEvent.addAttribute(DURATION, String.valueOf(stopWatchForMonitoring.getTime()));
        techEvent.publish();
    }

    private boolean resourceWasNotExisting(String decodedOriginalContent) {

        return decodedOriginalContent == null;
    }

    private boolean contentIsSame(String decodedOriginalContent, String newContent) {
        return newContent != null && newContent.equals(decodedOriginalContent);
    }

    private Optional<PullRequest> createPrOnBranch(Repository impactedRepo, Reference prBranch,String targetBranchForPr, BulkActionToPerform action) {

        PullRequestToCreate newPr = new PullRequestToCreate();
        newPr.setHead(prBranch.getBranchName());
        newPr.setBase(targetBranchForPr);

        PullRequestGitHubInteraction pullRequestGitHubInteraction=(PullRequestGitHubInteraction)action.getGitHubInteraction();

        String providedPrTitle=pullRequestGitHubInteraction.getPullRequestTitle();

        newPr.setTitle(providedPrTitle!=null ? providedPrTitle : prBranch.getBranchName());
        newPr.setBody("performed on behalf of " + action.getUserRequestingAction().getLogin() + " by CI-droid\n\n" + action.getCommitMessage());

        try{
            return Optional.of(remoteSourceControl.createPullRequest(impactedRepo.getFullName(), newPr, action.getSourceControlPersonalToken()));
        }
        catch(RemoteSourceControlAuthorizationException e){
            log.warn("issue while creating the PR",e);
            return Optional.empty();
        }
        catch(Exception e){
            log.warn("unknown issue while creating PR",e);
            return Optional.empty();
        }
    }

}
