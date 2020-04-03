package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes={ InfraConfig.class,LiveTestConfig.class}, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
@Disabled("to launch manually and test in local on 'real' pushEvent documents")
public class GithubEventListenerLIVETest {

    @Autowired
    SourceControlEventListener sourceControlEventListener;

    @Test
    public void actualLiveTest() throws Exception {

        String pushEventPayload = IOUtils
                .toString(GithubEventListenerLIVETest.class.getClassLoader().getResourceAsStream("pushEventLive.json"), "UTF-8");

        sourceControlEventListener.onPushEventOnDefaultBranch(pushEventPayload);

    }

}