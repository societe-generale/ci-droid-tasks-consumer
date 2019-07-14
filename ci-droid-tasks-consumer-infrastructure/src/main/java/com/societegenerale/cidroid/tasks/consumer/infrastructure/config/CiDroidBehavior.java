package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
@Data
@ConfigurationProperties(prefix = "cidroid-behavior")
public class CiDroidBehavior {

    private Map<String, String> patternToResourceMapping;

    private int maxFilesInPr = Integer.MAX_VALUE;

    private String maxFilesInPRExceededWarningMessage = EMPTY;

}