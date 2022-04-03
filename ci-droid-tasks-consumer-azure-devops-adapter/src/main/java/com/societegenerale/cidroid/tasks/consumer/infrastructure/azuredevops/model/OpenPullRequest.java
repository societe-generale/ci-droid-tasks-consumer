package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Data;

@Data
public class OpenPullRequest {

  private int pullRequestId;

  private String sourceRefName;
  
}
