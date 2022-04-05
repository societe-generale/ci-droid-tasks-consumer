package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Data;

@Data
public
class FileMetadata {

  private String commitId;

  private String url;
}
