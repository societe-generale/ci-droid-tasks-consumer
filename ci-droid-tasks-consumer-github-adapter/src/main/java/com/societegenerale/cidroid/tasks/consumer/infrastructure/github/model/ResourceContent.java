package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

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

    public com.societegenerale.cidroid.tasks.consumer.services.model.ResourceContent toStandardResourceContent() {

        return com.societegenerale.cidroid.tasks.consumer.services.model.ResourceContent.builder()
                .sha(this.sha)
                .base64EncodedContent(this.base64EncodedContent)
                .htmlLink(this.htmlLink)
                .build();

    }
}
