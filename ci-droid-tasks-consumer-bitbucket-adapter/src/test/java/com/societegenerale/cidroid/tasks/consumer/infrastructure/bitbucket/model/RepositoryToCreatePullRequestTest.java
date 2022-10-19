package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.repositoryToCreatePullRequest;
import static org.assertj.core.api.Assertions.assertThat;

class RepositoryToCreatePullRequestTest {

    @Test
    void build_standard_repo_from_bitbucket_repo() {
        var master = repositoryToCreatePullRequest().toStandardRepo("master").get();

        assertThat(master.isFork()).isTrue();
        assertThat(master.getId()).isEqualTo(1);
        assertThat(master.getName()).isEqualTo("ci-droid-consumer");
        assertThat(master.getFullName()).isEqualTo("ci-droid-consumer");
        assertThat(master.getCloneUrl()).isEqualTo("clone url");
        assertThat(master.getDefaultBranch()).isEqualTo("master");
    }
}