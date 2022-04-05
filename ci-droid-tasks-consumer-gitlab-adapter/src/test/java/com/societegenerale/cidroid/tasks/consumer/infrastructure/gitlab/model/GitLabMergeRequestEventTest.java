package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.TestUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitLabMergeRequestEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = TestUtils.readFromInputStream(getClass().getResourceAsStream("/mergeRequestEventGitLab.json"));

        GitLabMergeRequestEvent mergeRequestEvent = new ObjectMapper().readValue(pushEventPayload, GitLabMergeRequestEvent.class);

        assertThat(mergeRequestEvent).isNotNull();

        assertThat(mergeRequestEvent.getRepository()).isNotNull();
        assertThat(mergeRequestEvent.getRepository().getFullName()).isNotNull();
        assertThat(mergeRequestEvent.getRepository().getDefaultBranch()).isEqualTo("master");

        assertThat(mergeRequestEvent.getPrNumber()).isEqualTo(99);

    }


}
