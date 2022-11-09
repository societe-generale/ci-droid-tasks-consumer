package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.PullRequestToCreate.from;
import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.pullRequestToCreate;
import static org.assertj.core.api.Assertions.assertThat;

class PullRequestToCreateTest {

    @Test
    void build_bitbucket_pull_request_from_standard_pull_request() {
        var bitbucketPullRequestToCreate = from(pullRequestToCreate(), "ci-droid-consumer", new Project("ci-droid"));

        assertThat(bitbucketPullRequestToCreate.getTitle()).isEqualTo("pull request for feature 1");
        assertThat(bitbucketPullRequestToCreate.getDescription()).isEqualTo("pull request for feature 1");
        assertThat(bitbucketPullRequestToCreate.isOpen()).isTrue();
        assertThat(bitbucketPullRequestToCreate.isClosed()).isFalse();
        assertThat(bitbucketPullRequestToCreate.getFromRef().getRepository().getProject().getKey()).isEqualTo("ci-droid");
        assertThat(bitbucketPullRequestToCreate.getFromRef().getRepository().getSlug()).isEqualTo("ci-droid-consumer");
        assertThat(bitbucketPullRequestToCreate.getToRef().getRepository().getSlug()).isEqualTo("ci-droid-consumer");
        assertThat(bitbucketPullRequestToCreate.getToRef().getRepository().getProject().getKey()).isEqualTo("ci-droid");
        assertThat(bitbucketPullRequestToCreate.getFromRef().getId()).isEqualTo("feature");
        assertThat(bitbucketPullRequestToCreate.getToRef().getId()).isEqualTo("master");
    }
}
