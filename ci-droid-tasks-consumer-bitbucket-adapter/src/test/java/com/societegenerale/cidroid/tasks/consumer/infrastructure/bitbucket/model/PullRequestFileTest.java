package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.pullRequestFile;
import static org.assertj.core.api.Assertions.assertThat;

class PullRequestFileTest {

    @Test
    void build_Standard_PullRequest_File() {
        var pullRequestFile = pullRequestFile().toStandardPullRequestFile();
        assertThat(pullRequestFile.getSha()).isEqualTo("content_id");
        assertThat(pullRequestFile.getFilename()).isEqualTo("docker");
    }
}