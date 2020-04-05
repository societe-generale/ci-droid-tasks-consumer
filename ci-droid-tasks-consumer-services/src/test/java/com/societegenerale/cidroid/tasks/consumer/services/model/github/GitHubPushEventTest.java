package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static org.assertj.core.api.Assertions.assertThat;

class GitHubPushEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/pushEvent.json"));

        GitHubPushEvent pushEvent = new ObjectMapper().readValue(pushEventPayload, GitHubPushEvent.class);

        assertThat(pushEvent).isNotNull();

        assertThat(pushEvent.getRepository()).isNotNull();
        assertThat(pushEvent.getRepository().getDefaultBranch()).isEqualTo("master");

        assertThat(pushEvent.getUserEmail()).isEqualTo("baxterthehacker@users.noreply.github.com");
        assertThat(pushEvent.getUserName()).isEqualTo("baxterthehacker");

        assertThat(pushEvent.getRef()).isEqualTo("refs/heads/master");

        assertThat(pushEvent.getCommits()).isNotEmpty();

        assertThat(pushEvent.getCommits()).extracting("sha").isNotNull();
        assertThat(pushEvent.getCommits()).extracting("url").isNotNull();
        assertThat(pushEvent.getCommits()).extracting("author").isNotNull();
    }

}