package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.api.IssueProvidingContentException;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.api.gitHubInteractions.DirectPushGitHubInteraction;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.GitHubAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.*;
import com.societegenerale.cidroid.tasks.consumer.services.monitoring.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static com.societegenerale.cidroid.tasks.consumer.services.MonitoringAttributes.PR_NUMBER;
import static com.societegenerale.cidroid.tasks.consumer.services.MonitoringAttributes.REPO;
import static com.societegenerale.cidroid.tasks.consumer.services.MonitoringEvents.BULK_ACTION_COMMIT_PERFORMED;
import static com.societegenerale.cidroid.tasks.consumer.services.MonitoringEvents.BULK_ACTION_PR_CREATED;

@Slf4j
public class ActionToPerformService {

    private RemoteGitHub remoteGitHub;

    private ActionNotificationService actionNotificationService;

    public ActionToPerformService(RemoteGitHub remoteGitHub, ActionNotificationService actionNotificationService) {
        this.remoteGitHub = remoteGitHub;
        this.actionNotificationService = actionNotificationService;
    }

    public void perform(BulkActionToPerform action) {

        ResourceToUpdate resourceToUpdate=null;

        try {
            //we're supposed to have only one element in list, but this may change in the future : we'll loop over them.
            resourceToUpdate = action.getResourcesToUpdate().get(0);

            String repoFullName = resourceToUpdate.getRepoFullName();

            if (action.getGitHubInteraction() instanceof DirectPushGitHubInteraction) {

                UpdatedResource updatedResource = updateRemoteResource(repoFullName, resourceToUpdate, action, resourceToUpdate.getBranchName());

                actionNotificationService.handleNotificationsFor(action, resourceToUpdate, updatedResource);

            } else if (action.getGitHubInteraction() instanceof PullRequestGitHubInteraction) {

                PullRequestGitHubInteraction pullRequestAction = (PullRequestGitHubInteraction) action.getGitHubInteraction();

                Optional<Repository> optionalImpactedRepo = remoteGitHub.fetchRepository(repoFullName);

                //if repo doesn't exist, notify
                if(!optionalImpactedRepo.isPresent()){
                    actionNotificationService.handleNotificationsFor(action, resourceToUpdate, UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_REPO_DOESNT_EXIST));
                    return;
                }

                Repository impactedRepo=optionalImpactedRepo.get();

                String branchNameForPR = pullRequestAction.getBranchNameToCreate();

                String branchFromWhichToCreatePrBranch= resourceToUpdate.getBranchName() == null ? impactedRepo.getDefaultBranch() : resourceToUpdate.getBranchName();

                Reference masterCommitReference = remoteGitHub.fetchHeadReferenceFrom(repoFullName,branchFromWhichToCreatePrBranch );

                Reference branchToUseForPr = null;

                try {
                    branchToUseForPr = remoteGitHub.createBranch(repoFullName, branchNameForPR, masterCommitReference.getObject().getSha(),
                            action.getGitHubOauthToken());
                } catch (BranchAlreadyExistsException e) {

                    log.warn("branch " + branchNameForPR + " already exists");

                    //TODO maybe we should add field in Reference to identify when it hasn't been created as expected
                    branchToUseForPr = remoteGitHub.fetchHeadReferenceFrom(repoFullName, branchNameForPR);
                }

                UpdatedResource updatedResource;

                if (branchToUseForPr == null) {
                    //TODO test this scenario
                    updatedResource = UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_BRANCH_CREATION_ISSUE);

                } else {
                    updatedResource = updateRemoteResource(repoFullName, resourceToUpdate, action, branchNameForPR);

                    if (updatedResource.hasBeenUpdated()) {

                        //branchFromWhichToCreatePrBranch is also the target for the PR to be merged to
                        createPullRequest(action, impactedRepo, branchToUseForPr, branchFromWhichToCreatePrBranch, updatedResource);
                    }
                }

                actionNotificationService.handleNotificationsFor(action, resourceToUpdate, updatedResource);
            } else {
                log.warn("unknown gitHub interaction type : {}", action.getGitHubInteraction());
            }

        } catch (GitHubAuthorizationException e) {
            actionNotificationService.handleNotificationsFor(action, resourceToUpdate, UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_AUTHENTICATION_ISSUE));
        }
        catch (Exception e) {
            actionNotificationService.handleNotificationsFor(action, resourceToUpdate, UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_UNEXPECTED_EXCEPTION_DURING_PROCESSING));
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

                Event techEvent = Event.technical(BULK_ACTION_PR_CREATED);
                techEvent.addAttribute(REPO, impactedRepo.getFullName());
                techEvent.addAttribute("targetBranchForPR", targetBranchForPR);
                techEvent.addAttribute(PR_NUMBER, String.valueOf(createdPr.get().getNumber()));
                techEvent.publish();

            } else {
                //TODO test this scenario
                updatedResource.setUpdateStatus(UpdatedResource.UpdateStatus.UPDATE_OK_BUT_PR_CREATION_KO);
            }
        }

    }

    private Optional<PullRequest> findExistingOpenPRforBranch(Repository repo, Reference prBranch) {

        List<PullRequest> openPRs=remoteGitHub.fetchOpenPullRequests(repo.getFullName());

        return openPRs.stream().filter(pr -> pr.doneOnBranch(prBranch.getBranchName())).findAny();

    }

    private UpdatedResource updateRemoteResource(String repoFullName, ResourceToUpdate resourceToUpdate, BulkActionToPerform action,
            String onBranch) throws GitHubAuthorizationException {

        ResourceContent existingResourceContent = remoteGitHub
                .fetchContent(repoFullName, resourceToUpdate.getFilePathOnRepo(), onBranch);

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

    private boolean existingResourceExists(ResourceContent existingResourceContent) {
        return existingResourceContent != null && existingResourceContent.getSha() != null;
    }

    private void logWhatHasBeenDone(String repoFullName, ResourceToUpdate resourceToUpdate, String onBranch, ResourceContent existingResourceContent,
            String decodedOriginalContent, UpdatedResource updatedResource) {

        if (resourceWasNotExisting(decodedOriginalContent)) {

            log.info("{} created on repo {}, on branch {}. SHA1: {}", resourceToUpdate.getFilePathOnRepo(), repoFullName, onBranch,
                    updatedResource.getCommit().getSha());
        } else {
            log.info("{} updated on repo {}, on branch {}. old SHA1: {} . new SHA1: {}", resourceToUpdate.getFilePathOnRepo(), repoFullName, onBranch,
                    updatedResource.getCommit().getSha(), existingResourceContent.getSha());
        }
    }

    private UpdatedResource commitResource(BulkActionToPerform action, String newContent, ResourceToUpdate resourceToUpdate,
            ResourceContent existingResourceContent, String onBranch) throws GitHubAuthorizationException {

        DirectCommit directCommit = new DirectCommit();

        if (existingResourceExists(existingResourceContent)) {
            directCommit.setPreviousVersionSha1(existingResourceContent.getSha());
        }

        directCommit.setBranch(onBranch);
        directCommit.setCommitter(new DirectCommit.Committer(action.getUserRequestingAction().getLogin(), action.getEmail()));
        directCommit.setCommitMessage(action.getCommitMessage() + " performed on behalf of " + action.getUserRequestingAction().getLogin() + " by CI-droid");

        directCommit.setBase64EncodedContent(GitHubContentBase64codec.encode(newContent));

        UpdatedResource updatedResource = remoteGitHub
                .updateContent(resourceToUpdate.getRepoFullName(), resourceToUpdate.getFilePathOnRepo(), directCommit,
                        action.getGitHubOauthToken());

        publishMonitoringEvent(resourceToUpdate, updatedResource);

        updatedResource.setUpdateStatus(UpdatedResource.UpdateStatus.UPDATE_OK);

        return updatedResource;
    }

    private void publishMonitoringEvent(ResourceToUpdate resourceToUpdate, UpdatedResource updatedResource) {
        Event techEvent = Event.technical(BULK_ACTION_COMMIT_PERFORMED);
        techEvent.addAttribute(REPO, resourceToUpdate.getRepoFullName());
        techEvent.addAttribute("resourceName", resourceToUpdate.getFilePathOnRepo());
        techEvent.addAttribute("branchName", resourceToUpdate.getBranchName());
        techEvent.addAttribute("newCommitSha", updatedResource.getCommit().getSha());
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
            return Optional.of(remoteGitHub.createPullRequest(impactedRepo.getFullName(), newPr, action.getGitHubOauthToken()));
        }
        catch(GitHubAuthorizationException e){
            log.warn("issue while creating the PR",e);
            return Optional.empty();
        }
    }

}
