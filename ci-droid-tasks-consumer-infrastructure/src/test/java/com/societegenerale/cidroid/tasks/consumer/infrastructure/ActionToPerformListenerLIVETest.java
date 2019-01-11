package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {InfraConfig.class, MailSenderAutoConfiguration.class, LiveTestForActionToPerformListenerConfig.class}, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
@Ignore("to launch manually and test in local on 'real' actionToPerform documents")
public class ActionToPerformListenerLIVETest {

    @Autowired
    private ActionToPerformListener actionToPerformListener;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void actualLiveTest() throws Exception {

        ActionToPerformCommand actionToPerform = objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("actionToPerformLive.json"),
                ActionToPerformCommand.class);

        actionToPerformListener.onActionToPerform(actionToPerform);

    }

}