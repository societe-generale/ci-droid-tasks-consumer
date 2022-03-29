package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Data;

@Data
class OpenPullRequest {

  private int pullRequestId;

  private String sourceRefName;
  
}
