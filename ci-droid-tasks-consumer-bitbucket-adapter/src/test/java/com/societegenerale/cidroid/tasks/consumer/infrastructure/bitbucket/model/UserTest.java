package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util.TestUtils.getUser;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void build_StandardUser() {
        var user = getUser().toStandardUser();
        assertThat(user.getEmail()).isEqualTo("some.mail@gmail.com");
        assertThat(user.getLogin()).isEqualTo("sekhar");
    }
}