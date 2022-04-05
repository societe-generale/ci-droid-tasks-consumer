package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PullRequest pullRequest;

    @BeforeEach
    public void setUp() throws Exception {
        String pullRequestAsString = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("singlePullRequest.json"), StandardCharsets.UTF_8);

        pullRequest = objectMapper.readValue(pullRequestAsString, PullRequest.class);
    }

    @Test
    public void fieldsAreDeserialized() {
        assertThat(pullRequest).isNotNull();
        assertThat(pullRequest.getNumber()).isGreaterThan(0);
    }

    @Test
    public void dateIsCorrectlyDeserialized() {
        LocalDateTime expectedDate = LocalDateTime.of(2011, 1, 26, 19, 1, 12);
        assertThat(pullRequest.getCreationDate()).isEqualTo(expectedDate);
    }


}
