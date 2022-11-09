package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.reference;
import static org.assertj.core.api.Assertions.assertThat;

class ReferenceTest {

    @Test
    void build_StandardReference_from_bitbucket_reference() {
        var reference = reference().toStandardReference("reference");
        assertThat(reference.getRef()).isEqualTo("reference");
        assertThat(reference.getObject().getSha()).isEqualTo("sha key");
    }
}