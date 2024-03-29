package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.extensions.actionToReplicate.SimpleReplaceAction;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformCommand;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionToPerformCommandTest {



    @Test
    void toActionForSingleResource() {

        String someSCMApiToken = "someSCMApiToken";
        String someEmail = "someEmail";
        String someCommitMessage = "some commit message";
        SimpleReplaceAction someActionToPerform = new SimpleReplaceAction("initialValue", "newValue");
        PullRequestGitHubInteraction someInteractionType = new PullRequestGitHubInteraction("branchName", "prTitle");

        ActionToPerformCommand commandFor2resources=ActionToPerformCommand.builder()
            .sourceControlPersonalToken(someSCMApiToken)
            .email(someEmail)
            .commitMessage(someCommitMessage)
            .updateAction(someActionToPerform)
            .gitHubInteractionType(someInteractionType)
            .resourcesToUpdate(Collections.emptyList())
            .build();

        ResourceToUpdate targetResource= new ResourceToUpdate("repoFullName","filePathOnRepo","branchName","placeHolderValue");

        ActionToPerformCommand actualAction = commandFor2resources.toActionForSingleResource(targetResource);

        assertThat(actualAction).isNotNull();
        assertThat(actualAction.getSourceControlPersonalToken()).isEqualTo(someSCMApiToken);
        assertThat(actualAction.getEmail()).isEqualTo(someEmail);
        assertThat(actualAction.getCommitMessage()).isEqualTo(someCommitMessage);
        assertThat(actualAction.getGitHubInteractionType()).isEqualTo(someInteractionType);
        assertThat(actualAction.getUpdateAction()).isEqualTo(someActionToPerform);
        assertThat(actualAction.getResourcesToUpdate()).containsExactly(targetResource);

    }

    @Test
    void shouldMapPullRequestGitHubInteraction() throws IOException {

        String incomingCommandAsString = IOUtils
                .toString(ActionToPerformCommandTest.class.getClassLoader()
                                .getResourceAsStream("incomingOverWriteStaticContentAction_withPullRequestInteraction.json"),
                        StandardCharsets.UTF_8);

        ActionToPerformCommand incomingCommand = new ObjectMapper().readValue(incomingCommandAsString, ActionToPerformCommand.class);

        assertThat(incomingCommand.getGitHubInteractionType()).isInstanceOf(PullRequestGitHubInteraction.class);

        PullRequestGitHubInteraction githubInteraction = (PullRequestGitHubInteraction) incomingCommand.getGitHubInteractionType();

        assertThat(githubInteraction.getBranchNameToCreate()).isEqualTo("someBranchName");

    }


}
