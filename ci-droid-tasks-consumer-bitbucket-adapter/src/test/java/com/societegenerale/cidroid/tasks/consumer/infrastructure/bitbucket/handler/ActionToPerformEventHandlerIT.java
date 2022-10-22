package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.TestConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.config.BitbucketConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.mocks.BitBucketMockServer;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.mocks.BitBucketMockServer.BITBUCKET_MOCK_PORT;

@SpringBootTest
@ContextConfiguration(classes = { InfraConfig.class, TestConfig.class, BitbucketConfig.class })
@ActiveProfiles("test")
public abstract class ActionToPerformEventHandlerIT {

    @Autowired
    protected ActionToPerformListener actionToPerformListener;

    @Autowired
    private BitBucketMockServer bitbucketMockServer;

    protected MockServerClient bitBucketMockClient;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp(){
        bitbucketMockServer.start();

        bitBucketMockClient = new MockServerClient("localhost", BITBUCKET_MOCK_PORT);

    }

    @AfterEach
    public void tearDown() {
        bitbucketMockServer.stop();
    }

    protected Object getObjectFromJson(String fileName, Class<?> clazz) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(fileName), clazz);
    }

}
