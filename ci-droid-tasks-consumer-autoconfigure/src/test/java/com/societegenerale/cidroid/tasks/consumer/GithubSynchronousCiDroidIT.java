package com.societegenerale.cidroid.tasks.consumer;

import com.societegenerale.cidroid.tasks.consumer.autoconfigure.CiDroidTasksConsumerApplication;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.rest.GitHubSourceControlEventController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {CiDroidTasksConsumerApplication.class})
@ContextConfiguration(classes={ TestConfig.class})
@ActiveProfiles("github-synchronous-test")
@TestPropertySource(properties = { "source-control.apiToken = dummyToken"})
public class GithubSynchronousCiDroidIT {

    @Autowired
    ApplicationContext appContext;

    @Autowired
    GitHubSourceControlEventController gitHubSourceControlEventController;

    @Test
    void canStart() {
      assertThat(appContext).isNotNull();
      assertThat(gitHubSourceControlEventController).isNotNull();
    }
}
