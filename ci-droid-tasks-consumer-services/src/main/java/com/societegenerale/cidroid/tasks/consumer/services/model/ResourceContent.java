package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceContent {

    private String sha;

    private String base64EncodedContent;

    private String htmlLink;
}
