package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.repository;
import static org.assertj.core.api.Assertions.assertThat;

class BitbucketPullRequestEventTest {

    @Test
    void get_standard_repo_from_bitbucket_repository() {
        Repository master = repository();
        assertThat(new BitbucketPullRequestEvent("created", master, 1).getRepository()
                .getDefaultBranch()).isEqualTo("master");
    }



}