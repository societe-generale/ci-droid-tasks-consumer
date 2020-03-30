package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static org.assertj.core.api.Assertions.assertThat;

class GitHubPullRequestEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/pullRequestEvent.json"));

        GitHubPullRequestEvent pullRequestEvent = new ObjectMapper().readValue(pushEventPayload, GitHubPullRequestEvent.class);

        assertThat(pullRequestEvent).isNotNull();

        assertThat(pullRequestEvent.getRepository()).isNotNull();
        assertThat(pullRequestEvent.getRepository().getDefaultBranch()).isEqualTo("master");

        assertThat(pullRequestEvent.getAction()).isEqualTo("opened");

        assertThat(pullRequestEvent.getPrNumber()).isEqualTo(1);
    }
}