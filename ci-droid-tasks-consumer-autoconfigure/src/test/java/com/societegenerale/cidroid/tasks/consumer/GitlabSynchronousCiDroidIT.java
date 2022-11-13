package com.societegenerale.cidroid.tasks.consumer;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.rest.GitLabSourceControlEventController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes={ TestConfig.class})
@ActiveProfiles("gitlab-synchronous-test")
public class GitlabSynchronousCiDroidIT {

    @Autowired
    ApplicationContext appContext;

    @Autowired
    GitLabSourceControlEventController gitLabSourceControlEventController;

    @Test
    void canStart() {
      assertThat(appContext).isNotNull();
      assertThat(gitLabSourceControlEventController).isNotNull();
    }
}
