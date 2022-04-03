package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public
class PullRequestCreation {

  private final String sourceRefName;

  private final String targetRefName;

  private final String title;

}
