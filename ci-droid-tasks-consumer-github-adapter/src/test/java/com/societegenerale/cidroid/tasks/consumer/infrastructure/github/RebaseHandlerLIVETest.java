package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.RebaseHandler;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = { InfraConfig.class, LiveTestConfig.class }, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
@Disabled("to launch manually and test in local")
class RebaseHandlerLIVETest {

    @Autowired
    Rebaser rebaser;

    @Autowired
    SourceControlEventsReactionPerformer remoteSourceControl;

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

        rebaseHandler = new RebaseHandler(rebaser, remoteSourceControl);
    }

    @Test
    void manualTest() {

        rebaseHandler.handle(pushEvent, List.of(singlePr));
    }

}
