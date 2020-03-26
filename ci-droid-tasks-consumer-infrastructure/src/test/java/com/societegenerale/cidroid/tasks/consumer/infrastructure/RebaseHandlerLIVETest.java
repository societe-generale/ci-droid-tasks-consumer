package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.RebaseHandler;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { InfraConfig.class, LiveTestConfig.class }, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
@Disabled("to launch manually and test in local")
public class RebaseHandlerLIVETest {

    @Autowired
    Rebaser rebaser;

    @Autowired
    RemoteGitHub remoteGitHub;

    PullRequest singlePr;

    PushEvent pushEvent;

    RebaseHandler rebaseHandler;

    @BeforeEach
    public void setUp() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        String prAsString = IOUtils
                .toString(RebaseHandlerLIVETest.class.getClassLoader().getResourceAsStream("myPullRequestToRebase.json"), "UTF-8");
        singlePr = objectMapper.readValue(prAsString, PullRequest.class);

        String pushEventPayload = IOUtils
                .toString(RebaseHandlerLIVETest.class.getClassLoader().getResourceAsStream("myPushEventToTest.json"), "UTF-8");

        pushEvent = objectMapper.readValue(pushEventPayload, PushEvent.class);

        rebaseHandler = new RebaseHandler(rebaser, remoteGitHub);
    }

    @Test
    public void manualTest() {

        rebaseHandler.handle(pushEvent, Arrays.asList(singlePr));
    }

}