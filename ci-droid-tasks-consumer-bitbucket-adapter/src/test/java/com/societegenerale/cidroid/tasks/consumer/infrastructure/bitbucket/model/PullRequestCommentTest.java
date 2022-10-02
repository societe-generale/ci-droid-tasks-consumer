package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.pullRequestComment;
import static org.assertj.core.api.Assertions.assertThat;

class PullRequestCommentTest {

    @Test
    void build_standard_Pull_Request_Comment() {
        var pullRequestComment = pullRequestComment().toStandardPullRequestComment();

        assertThat(pullRequestComment.getAuthor().getLogin()).isEqualTo("sekhar");
        assertThat(pullRequestComment.getComment()).isEqualTo("committed by ci droid");

    }
}