package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.TestUtils;
import java.io.IOException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PullRequestCommentTest {

    @Test
    void shouldDeserializeCorrectly() throws IOException {

        String pullRequestCommentsAsString = TestUtils.readFromInputStream(getClass().getResourceAsStream("/pullRequestComments.json"));
        List<PullRequestComment> prComments = new ObjectMapper()
                .readValue(pullRequestCommentsAsString, new TypeReference<List<PullRequestComment>>() {
                });

        Assertions.assertThat(prComments).hasSize(1);

        PullRequestComment firstPrComment = prComments.get(0);
        assertThat(firstPrComment.getComment()).isEqualTo("Great stuff!");
        assertThat(firstPrComment.getAuthor().getLogin()).isEqualTo("octocat");
    }

}
