package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PushEventTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void fieldsAreDeserialized() throws IOException {

        String pushEventAsString = IOUtils
                . toString(PushEventTest.class.getClassLoader().getResourceAsStream("pushEvent.json"), StandardCharsets.UTF_8);

        PushEvent pushEvent = objectMapper.readValue(pushEventAsString, GitHubPushEvent.class);

        assertThat(pushEvent).isNotNull();

    }

}
