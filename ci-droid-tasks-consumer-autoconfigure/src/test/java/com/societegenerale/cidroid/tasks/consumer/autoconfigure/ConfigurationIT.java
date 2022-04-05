package com.societegenerale.cidroid.tasks.consumer.autoconfigure;

import java.lang.reflect.Field;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestSizeCheckHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;


@ContextConfiguration(classes = {InfraConfig.class})
@SpringBootTest
@Slf4j
public class ConfigurationIT {
    @Autowired
    private PullRequestSizeCheckHandler pullRequestSizeCheckHandlers;

    @Value("${cidroid-behavior.maxFilesInPRNotifier.warningMessage}")
    private String maxFieldsWarningMessageConfigured;

    @Value("${cidroid-behavior.maxFilesInPRNotifier.maxFiles}")
    private int maxFilesConfigured;

    @Test
    public void shouldCreatePullRequestSizeCheckHandlersWithConfiguredValues() {

        assertThat(pullRequestSizeCheckHandlers).isNotNull();

        Object maxFilesInPr = getPrivateFieldValue(pullRequestSizeCheckHandlers, "maxFilesInPr");
        assertThat(maxFilesInPr).isEqualTo(maxFilesConfigured);

        Object maxFilesWarningMessage = getPrivateFieldValue(pullRequestSizeCheckHandlers, "maxFilesWarningMessage");
        assertThat(maxFilesWarningMessage).isEqualTo(maxFieldsWarningMessageConfigured);
    }

    private Object getPrivateFieldValue(PullRequestSizeCheckHandler pullRequestSizeCheckHandlers, String fieldName) {
        Object fieldValue = null;
        try {
            Field field = pullRequestSizeCheckHandlers.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldValue = field.get(pullRequestSizeCheckHandlers);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            log.warn("Error extracting field ", exception);
        }
        return fieldValue;
    }


}
