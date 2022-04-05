package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.TestUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubPushEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = TestUtils.readFromInputStream(getClass().getResourceAsStream("/pushEvent.json"));

        GitHubPushEvent pushEvent = new ObjectMapper().readValue(pushEventPayload, GitHubPushEvent.class);

        assertThat(pushEvent).isNotNull();

        assertThat(pushEvent.getRepository()).isNotNull();
        assertThat(pushEvent.getRepository().getDefaultBranch()).isEqualTo("master");

        assertThat(pushEvent.getUserEmail()).isEqualTo("baxterthehacker@users.noreply.github.com");
        assertThat(pushEvent.getUserName()).isEqualTo("baxterthehacker");

        assertThat(pushEvent.getRef()).isEqualTo("refs/heads/master");

        assertThat(pushEvent.getCommits()).isNotEmpty();

        assertThat(pushEvent.getCommits()).extracting("id").isNotEmpty();
        assertThat(pushEvent.getCommits()).extracting("url").isNotEmpty();
        assertThat(pushEvent.getCommits()).extracting("author").isNotEmpty();
    }

}
