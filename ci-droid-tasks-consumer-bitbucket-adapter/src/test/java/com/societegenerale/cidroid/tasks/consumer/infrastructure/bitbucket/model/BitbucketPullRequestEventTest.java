package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.pullRequest;
import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.repository;
import static org.assertj.core.api.Assertions.assertThat;

class BitbucketPullRequestEventTest {

    @Test
    void get_standard_repo_from_bitbucket_repository() {
        var pullRequest = pullRequest();
        assertThat(new BitbucketPullRequestEvent("pr:opened", null, pullRequest).getRepository()
                .getFullName()).isEqualTo("ci-droid-consumer");
    }

    @Test
    void get_action_from_event_key() {
        var pullRequest = pullRequest();
        assertThat(new BitbucketPullRequestEvent("pr:opened", null, pullRequest).getAction()).isEqualTo("opened");
    }

    @Test
    void get_pr_number_from_pull_request() {
        var pullRequest = pullRequest();
        assertThat(new BitbucketPullRequestEvent("pr:opened", null, pullRequest).getPrNumber()).isEqualTo(1);
    }
}