package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Repository {

    private String url;

    private String defaultBranch;
    private String fullName;

    private String name;

    private boolean fork;

    private String cloneUrl;

    private int id;
}
