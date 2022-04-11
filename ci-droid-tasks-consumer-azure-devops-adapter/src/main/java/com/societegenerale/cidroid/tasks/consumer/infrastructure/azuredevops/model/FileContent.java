package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Data;

@Data
public
class FileContent {

  private String commitId;

  private String url;

  private String content;
}
