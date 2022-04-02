package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Data;

@Data
class PullRequestCreated {

  private int pullRequestId;

  private String sourceRefName;

  private String url;

}
