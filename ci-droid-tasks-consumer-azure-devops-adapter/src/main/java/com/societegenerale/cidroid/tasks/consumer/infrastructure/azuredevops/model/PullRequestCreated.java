package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Data;

@Data
public class PullRequestCreated {

  private int pullRequestId;

  private String sourceRefName;

  private String url;

}
