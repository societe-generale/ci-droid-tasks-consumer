package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.directCommit;
import static org.assertj.core.api.Assertions.assertThat;

class DirectCommitTest {

    @Test
    void build_bitbucket_commit_from_standard_commit() {
        var bitBucketDirectCommit = DirectCommit.from(directCommit());
        assertThat(bitBucketDirectCommit.getBranch()).isEqualTo("master");
        assertThat(bitBucketDirectCommit.getCommitMessage()).isEqualTo("committed by ci droid");
        assertThat(bitBucketDirectCommit.getContent()).isEqualTo("updated content");
        assertThat(bitBucketDirectCommit.getPreviousVersionSha1()).isEqualTo("previous sha");
    }
}