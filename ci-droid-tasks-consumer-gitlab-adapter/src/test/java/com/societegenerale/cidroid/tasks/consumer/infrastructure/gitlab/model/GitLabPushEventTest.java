package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.TestUtils;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitLabPushEventTest {

    @Test
    void canDeserialize() throws IOException {

        String  pushEventPayload = TestUtils.readFromInputStream(getClass().getResourceAsStream("/pushEventGitLab_fromWebsite.json"));

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

        Commit commit1=pushEvent.getCommits().get(0);

        assertThat(commit1.getId()).isEqualTo("b6568db1bc1dcd7f8b4d5a946b0b91f9dacd7327");
        assertThat(commit1.getUrl()).isEqualTo("http://example.com/mike/diaspora/commit/b6568db1bc1dcd7f8b4d5a946b0b91f9dacd7327");
        assertThat(commit1.getAuthor().getEmail()).isEqualTo("jordi@softcatala.org");

        assertThat(commit1.getAddedFiles()).containsExactly("CHANGELOG");
        assertThat(commit1.getModifiedFiles()).containsExactly("app/controller/application.rb");
        assertThat(commit1.getRemovedFiles()).containsExactlyInAnyOrder("test/Bla1.java","test/Bla2.java");
    }

    @Test
    void canDeserializeTestEvent() throws IOException {

        String  pushEventPayload = TestUtils.readFromInputStream(getClass().getResourceAsStream("/pushEventGitLab_test.json"));

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

    }

}
