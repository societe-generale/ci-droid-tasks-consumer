package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceContent {

    private String sha;

    @JsonProperty("content")
    private String base64EncodedContent;

    @JsonProperty("html_url")
    private String htmlLink;

}