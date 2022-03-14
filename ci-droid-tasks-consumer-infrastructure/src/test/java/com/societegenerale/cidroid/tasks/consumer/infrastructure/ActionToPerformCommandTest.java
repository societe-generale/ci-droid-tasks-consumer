package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.extensions.actionToReplicate.SimpleReplaceAction;
import java.io.IOException;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class ActionToPerformCommandTest {



    @Test
    void toActionForSingleResource() {

        String someOauthToken = "someOauthToken";
        String someEmail = "someEmail";
        String someCommitMessage = "some commit message";
        SimpleReplaceAction someActionToPerform = new SimpleReplaceAction("initialValue", "newValue");
        PullRequestGitHubInteraction someInteractionType = new PullRequestGitHubInteraction("branchName", "prTitle");

        ActionToPerformCommand commandFor2resources=ActionToPerformCommand.builder()
            .gitHubOauthToken(someOauthToken)
            .email(someEmail)
            .commitMessage(someCommitMessage)
            .updateAction(someActionToPerform)
            .gitHubInteractionType(someInteractionType)
            .resourcesToUpdate(Collections.emptyList())
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

    @Test
    void shouldMapPullRequestGitHubInteraction() throws IOException {

        String incomingCommandAsString = IOUtils
                .toString(ActionToPerformCommandTest.class.getClassLoader()
                                .getResourceAsStream("incomingOverWriteStaticContentAction_withPullRequestInteraction.json"),
                        "UTF-8");

        ActionToPerformCommand incomingCommand = new ObjectMapper().readValue(incomingCommandAsString, ActionToPerformCommand.class);

        assertThat(incomingCommand.getGitHubInteractionType()).isInstanceOf(PullRequestGitHubInteraction.class);

        PullRequestGitHubInteraction githubInteraction = (PullRequestGitHubInteraction) incomingCommand.getGitHubInteractionType();

        assertThat(githubInteraction.getBranchNameToCreate()).isEqualTo("someBranchName");

    }


}