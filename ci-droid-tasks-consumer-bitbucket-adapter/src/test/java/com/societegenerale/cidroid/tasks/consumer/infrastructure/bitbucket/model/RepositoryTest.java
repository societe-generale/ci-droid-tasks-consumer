package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.repository;
import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTest {

    @Test
    void build_standard_repository_from_bitbucket_repository() {
        var repository = repository().toStandardRepo().get();
        assertThat(repository.getCloneUrl()).isEqualTo("clone url");
        assertThat(repository.getDefaultBranch()).isEqualTo("master");
        assertThat(repository.getName()).isEqualTo("public-repo");
        assertThat(repository.getFullName()).isEqualTo("public-repo");
        assertThat(repository.getId()).isEqualTo( 0);
        assertThat(repository.isFork()).isTrue();
    }
}