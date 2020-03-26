package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PushEventTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void fieldsAreDeserialized() throws IOException {

        String pushEventAsString = IOUtils
                .toString(PushEventTest.class.getClassLoader().getResourceAsStream("pushEvent.json"), "UTF-8");

        PushEvent pushEvent = objectMapper.readValue(pushEventAsString, GitHubPushEvent.class);

        assertThat(pushEvent).isNotNull();

    }

}