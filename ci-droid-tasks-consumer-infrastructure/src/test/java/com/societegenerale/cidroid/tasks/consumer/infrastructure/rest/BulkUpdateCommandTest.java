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
        BulkUpdateCommand commandFor2resources=BulkUpdateCommand.builder()
                .gitHubOauthToken(someOauthToken)
                .email(someEmail)
                .commitMessage(someCommitMessage)
                .updateAction(new SimpleReplaceAction("initialValue","newValue"))
                .gitHubInteractionType(new PullRequestGitHubInteraction("branchName","prTitle"))
                .build();

        ResourceToUpdate targetResource= new ResourceToUpdate("repoFullName","filePathOnRepo","branchName","placeHolderValue");

        ActionToPerformCommand actualAction = commandFor2resources.toActionForSingleResource(targetResource);

        assertThat(actualAction).isNotNull();
        assertThat(actualAction.getGitHubOauthToken()).isEqualToIgnoringCase(someOauthToken);
        assertThat(actualAction.getEmail()).isEqualToIgnoringCase(someEmail);
        assertThat(actualAction.getCommitMessage()).isEqualToIgnoringCase(someCommitMessage);
    }
}
