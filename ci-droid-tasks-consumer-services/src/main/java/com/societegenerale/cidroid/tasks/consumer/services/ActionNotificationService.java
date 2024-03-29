package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.DirectPushGitHubInteraction;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;

import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_AUTHENTICATION_ISSUE;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_FILE_CONTENT_IS_SAME;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_FILE_DOESNT_EXIST;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_REPO_DOESNT_EXIST;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_UNEXPECTED_EXCEPTION_DURING_PROCESSING;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_OK_BUT_PR_CREATION_KO;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_OK_WITH_PR_ALREADY_EXISTING;
import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_OK_WITH_PR_CREATED;

public class ActionNotificationService {

    private final ActionNotifier notifier;

    public ActionNotificationService(ActionNotifier notifier) {
        this.notifier = notifier;
    }

    public void handleNotificationsFor(BulkActionToPerform action, ResourceToUpdate resourceToUpdate, UpdatedResource updatedResource) {

        User user = new User(action.getUserRequestingAction().getLogin(), action.getEmail());

        if (resourceToUpdate == null) {
            notifier.notify(user,
                    "[KO] unclear status for action " + action.getActionType(),
                    "resourceToUpdate is null, so unable to perform any action");

            return;
        }

        String repoFullName = resourceToUpdate.getRepoFullName();

        String notificationSubject = "Action '" + action.getActionType() + "' for " +
                resourceToUpdate.getFilePathOnRepo() + " on " +
                repoFullName + " on branch " + resourceToUpdate.getBranchName();

        if (updatedResource.getUpdateStatus() == UPDATE_KO_AUTHENTICATION_ISSUE) {
            notifier.notify(user,
                    "[KO] " + notificationSubject,
                    "Content hasn't been modified on repository, as we haven't been able to commit content due " +
                            "to an authorization issue. please double check the credentials you provided");
        } else if (updatedResource.getUpdateStatus() == UPDATE_KO_UNEXPECTED_EXCEPTION_DURING_PROCESSING) {
            notifier.notify(user,
                    "[KO] " + notificationSubject,
                    "Unexpected issue happened during processing - please check the logs for more details");
        } else if (updatedResource.getUpdateStatus() == UPDATE_KO_REPO_DOESNT_EXIST) {
            notifier.notify(user,
                    "[KO] " + notificationSubject,
                    "repository " + resourceToUpdate.getRepoFullName() +
                            " doesn't exist - make sure you provide its full name, ie 'org/repo' ");
        }

        //TODO refactor, as code in manageDirectPush and managePullRequest is very similar
        else if (action.getGitHubInteraction() instanceof DirectPushGitHubInteraction) {

            manageDirectPush(updatedResource, user, notificationSubject);

        } else if (action.getGitHubInteraction() instanceof PullRequestGitHubInteraction) {

            managePullRequest(updatedResource, user, notificationSubject);
        }

    }

    private void managePullRequest(UpdatedResource updatedResource, User user, String notificationSubject) {

        if (updatedResource.hasBeenUpdated()) {

            if (updatedResource.getUpdateStatus().equals(UPDATE_OK_WITH_PR_CREATED)) {

                notifier.notify(user,
                        "[OK] " + notificationSubject,
                        "CI-droid has created a PR on your behalf.\n you can double check its content here : " +
                                updatedResource.getContent().getHtmlUrl());
            } else if (updatedResource.getUpdateStatus().equals(UPDATE_OK_WITH_PR_ALREADY_EXISTING)) {

                notifier.notify(user,
                        "[OK] " + notificationSubject,
                        "CI-droid updated an existing PR on your behalf.\n you can double check its content here : " +
                                updatedResource.getContent().getHtmlUrl());
            } else if (updatedResource.getUpdateStatus().equals(UPDATE_OK_BUT_PR_CREATION_KO)) {
                //TODO
            }

        } else if (updatedResource.getUpdateStatus().equals(UPDATE_KO_FILE_CONTENT_IS_SAME)) {

            String notificationContent = "Content hasn't been modified on repository, " +
                    "as the new file content is the same as the previous one\n" +
                    "We haven't created a PR, but we may have created the branch though\n\n" +
                    "you can double check its content here : " +
                    updatedResource.getContent().getHtmlUrl();

            notifier.notify(user, "[KO] " + notificationSubject, notificationContent);

        } else if (updatedResource.getUpdateStatus().equals(UPDATE_KO_FILE_DOESNT_EXIST)) {

            String notificationContent = "Content hasn't been modified on repository, as the file to update doesn't exist\n" +
                    "We haven't created a PR, but we may have created the branch though\n\n";

            notifier.notify(user, "[KO] " + notificationSubject, notificationContent);
        }

    }

    private void manageDirectPush(UpdatedResource updatedResource, User user, String notificationSubject) {
        if (updatedResource.hasBeenUpdated()) {

            notifier.notify(user,
                    "[OK] " + notificationSubject,
                    "CI-droid has updated the resource on your behalf.\n Link to the version we committed : " +
                            updatedResource.getContent().getHtmlUrl());

        } else if (updatedResource.getUpdateStatus().equals(UPDATE_KO_FILE_CONTENT_IS_SAME)) {

            String notificationContent = "Content hasn't been modified on repository, " +
                    "as the new file content is the same as the previous one\n" +
                    "you can double check its content here : " +
                    updatedResource.getContent().getHtmlUrl();

            notifier.notify(user, "[KO] " + notificationSubject, notificationContent);

        } else if (updatedResource.getUpdateStatus().equals(UPDATE_KO_FILE_DOESNT_EXIST)) {

            String notificationContent =
                    "Content hasn't been modified on repository, as the file to update doesn't exist\n";

            notifier.notify(user, "[KO] " + notificationSubject, notificationContent);
        }
    }
}
