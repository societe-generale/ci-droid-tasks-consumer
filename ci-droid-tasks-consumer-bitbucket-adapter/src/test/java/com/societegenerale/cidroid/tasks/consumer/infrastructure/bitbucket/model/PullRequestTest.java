package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.pullRequest;
import static org.assertj.core.api.Assertions.assertThat;

class PullRequestTest {

    @Test
    void build_StandardPullRequest() {
        var pullRequest = pullRequest().toStandardPullRequest();
        var user = pullRequest.getUser();
        assertThat(user.getEmail()).isEqualTo("some.mail@gmail.com");
        assertThat(user.getLogin()).isEqualTo("sekhar");
        assertThat(pullRequest.getUrl()).isEqualTo("href");
        assertThat(pullRequest.getBranchStartedFromCommit()).isEqualTo("latest_commit");
        assertThat(pullRequest.getBaseBranchName()).isEqualTo("dis_id_toRef");
        assertThat(pullRequest.getBranchName()).isEqualTo("dis_id_fromRef");
        assertThat(pullRequest.getCreationDate()).isEqualTo(LocalDateTime.of(2022, 10, 15, 10, 41, 39));
        assertThat(pullRequest.getHtmlUrl()).isEqualTo("href");
        assertThat(pullRequest.getNumber()).isEqualTo(1);
        assertThat(pullRequest.getRepo().getFullName()).isEqualTo("ci-droid-consumer");
    }
}