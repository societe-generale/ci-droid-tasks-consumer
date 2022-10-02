package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.getSelfWithClone;
import static org.assertj.core.api.Assertions.assertThat;

class SelfWithCloneTest {

    @Test
    void get_http_url() {
        assertThat(getSelfWithClone().getHttpCloneURL()).isEqualTo("clone url");
    }

}