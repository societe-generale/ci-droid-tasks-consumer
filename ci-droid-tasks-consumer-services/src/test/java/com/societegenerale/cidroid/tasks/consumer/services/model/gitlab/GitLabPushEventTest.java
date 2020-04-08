package com.societegenerale.cidroid.tasks.consumer.services.model.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static org.assertj.core.api.Assertions.assertThat;

class GitLabPushEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/gitLab/pushEventGitLab_fromWebsite.json"));

        GitLabPushEvent pushEvent = new ObjectMapper().readValue(pushEventPayload, GitLabPushEvent.class);

        assertThat(pushEvent).isNotNull();

        assertThat(pushEvent.getRepository()).isNotNull();
        assertThat(pushEvent.getRepository().getFullName()).isNotNull();
        assertThat(pushEvent.getRepository().getDefaultBranch()).isEqualTo("master");
        assertThat(pushEvent.getRepository().getId()).isEqualTo(15);

        assertThat(pushEvent.getUserEmail()).isEqualTo("john@example.com");
        assertThat(pushEvent.getUserName()).isEqualTo("John Smith");

        assertThat(pushEvent.getRef()).isEqualTo("refs/heads/master");

        assertThat(pushEvent.getNbCommits()).isGreaterThan(0);

        assertThat(pushEvent.getCommits()).isNotEmpty();


        assertThat(pushEvent.getCommits()).extracting("sha").isNotNull();
        assertThat(pushEvent.getCommits()).extracting("url").isNotNull();
        assertThat(pushEvent.getCommits()).extracting("author").isNotNull();

    }

    @Test
    void canDeserializeTestEvent() throws IOException {

        String  pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/gitLab/pushEventGitLab_test.json"));

        GitLabPushEvent pushEvent = new ObjectMapper().readValue(pushEventPayload, GitLabPushEvent.class);

        assertThat(pushEvent).isNotNull();

        assertThat(pushEvent.getRepository()).isNotNull();
        assertThat(pushEvent.getRepository().getFullName()).isNotNull();
        assertThat(pushEvent.getRepository().getDefaultBranch()).isEqualTo("master");
        assertThat(pushEvent.getRepository().getId()).isEqualTo(16);

        assertThat(pushEvent.getUserEmail()).isEqualTo("john@socgen.com");
        assertThat(pushEvent.getUserName()).isEqualTo("John Smith");

        assertThat(pushEvent.getRef()).isEqualTo("refs/heads/master");

        assertThat(pushEvent.getNbCommits()).isGreaterThan(0);

        assertThat(pushEvent.getCommits()).isNotEmpty();

        assertThat(pushEvent.getCommits()).extracting("sha").isNotNull();
        assertThat(pushEvent.getCommits()).extracting("url").isNotNull();
        assertThat(pushEvent.getCommits()).extracting("author").isNotNull();

    }

}