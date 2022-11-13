package com.societegenerale.cidroid.tasks.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes={ TestConfig.class})
@ActiveProfiles("bitbucket-synchronous-test")
public class BitBucketCiDroidIT {

    @Autowired
    ApplicationContext appContext;

    @Test
    void canStart() {
      assertThat(appContext).isNotNull();
    }
}
