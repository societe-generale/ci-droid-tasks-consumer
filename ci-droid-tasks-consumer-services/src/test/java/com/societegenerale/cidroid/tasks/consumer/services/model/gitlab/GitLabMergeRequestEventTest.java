package com.societegenerale.cidroid.tasks.consumer.services.model.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.societegenerale.cidroid.tasks.consumer.services.TestUtils.readFromInputStream;
import static org.assertj.core.api.Assertions.assertThat;

class GitLabMergeRequestEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = readFromInputStream(getClass().getResourceAsStream("/mergeRequestEventGitLab.json"));

        GitLabMergeRequestEvent mergeRequestEvent = new ObjectMapper().readValue(pushEventPayload, GitLabMergeRequestEvent.class);

        assertThat(mergeRequestEvent).isNotNull();

        assertThat(mergeRequestEvent.getRepository()).isNotNull();
        assertThat(mergeRequestEvent.getRepository().getDefaultBranch()).isEqualTo("master");

        assertThat(mergeRequestEvent.getPrNumber()).isEqualTo(99);

    }


}