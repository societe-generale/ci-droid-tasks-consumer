package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.extensions.actionToReplicate.SimpleReplaceAction;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BulkUpdateCommandTest {

    @Test
    void toActionForSingleResource() {

        String someOauthToken = "someOauthToken";
        String someEmail = "someEmail";
        String someCommitMessage = "some commit message";
        SimpleReplaceAction someActionToPerform = new SimpleReplaceAction("initialValue", "newValue");
        PullRequestGitHubInteraction someInteractionType = new PullRequestGitHubInteraction("branchName", "prTitle");

        BulkUpdateCommand commandFor2resources=BulkUpdateCommand.builder()
                .gitHubOauthToken(someOauthToken)
                .email(someEmail)
                .commitMessage(someCommitMessage)
                .updateAction(someActionToPerform)
                .gitHubInteractionType(someInteractionType)
                .build();

        ResourceToUpdate targetResource= new ResourceToUpdate("repoFullName","filePathOnRepo","branchName","placeHolderValue");

        ActionToPerformCommand actualAction = commandFor2resources.toActionForSingleResource(targetResource);

        assertThat(actualAction).isNotNull();
        assertThat(actualAction.getGitHubOauthToken()).isEqualTo(someOauthToken);
        assertThat(actualAction.getEmail()).isEqualTo(someEmail);
        assertThat(actualAction.getCommitMessage()).isEqualTo(someCommitMessage);
        assertThat(actualAction.getGitHubInteractionType()).isEqualTo(someInteractionType);
        assertThat(actualAction.getUpdateAction()).isEqualTo(someActionToPerform);
        assertThat(actualAction.getResourcesToUpdate()).containsExactly(targetResource);

    }
}
