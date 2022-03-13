package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.rest.BulkUpdateCommand;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

public class ActionToPerformCommandTest {

    @Test
    public void shouldMapPullRequestGitHubInteraction() throws IOException {

        String incomingCommandAsString = IOUtils
                .toString(ActionToPerformCommandTest.class.getClassLoader()
                                .getResourceAsStream("incomingOverWriteStaticContentAction_withPullRequestInteraction.json"),
                        "UTF-8");

        BulkUpdateCommand incomingCommand = new ObjectMapper().readValue(incomingCommandAsString, BulkUpdateCommand.class);

        assertThat(incomingCommand.getGitHubInteractionType()).isInstanceOf(PullRequestGitHubInteraction.class);

        PullRequestGitHubInteraction githubInteraction = (PullRequestGitHubInteraction) incomingCommand.getGitHubInteractionType();

        assertThat(githubInteraction.getBranchNameToCreate()).isEqualTo("someBranchName");

    }

}