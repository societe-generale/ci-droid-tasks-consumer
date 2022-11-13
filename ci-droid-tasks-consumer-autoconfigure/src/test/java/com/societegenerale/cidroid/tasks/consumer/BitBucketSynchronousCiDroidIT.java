package com.societegenerale.cidroid.tasks.consumer;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.rest.BitBucketSourceControlEventController;
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
public class BitBucketSynchronousCiDroidIT {

    @Autowired
    ApplicationContext appContext;

    @Autowired
    BitBucketSourceControlEventController bitBucketSourceControlEventController;

    @Test
    void canStart() {
      assertThat(appContext).isNotNull();
      assertThat(bitBucketSourceControlEventController).isNotNull();
    }
}
