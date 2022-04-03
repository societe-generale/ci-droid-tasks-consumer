package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.TestUtils;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubPullRequestEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = TestUtils.readFromInputStream(getClass().getResourceAsStream("/pullRequestEvent.json"));

        PullRequestEvent pullRequestEvent = new ObjectMapper().readValue(pushEventPayload, GitHubPullRequestEvent.class);

        assertThat(pullRequestEvent).isNotNull();

        assertThat(pullRequestEvent.getRepository()).isNotNull();
        assertThat(pullRequestEvent.getRepository().getDefaultBranch()).isEqualTo("master");

        assertThat(pullRequestEvent.getAction()).isEqualTo("opened");

        assertThat(pullRequestEvent.getPrNumber()).isEqualTo(1);
    }
}
