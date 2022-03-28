package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Data;

@Data
public class FileMetadata {

  private String commitId;

  private String url;
}
