package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private PullRequest pullRequest;

    @BeforeEach
    public void setUp() throws Exception {
        String pullRequestAsString = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("singlePullRequest.json"), "UTF-8");

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