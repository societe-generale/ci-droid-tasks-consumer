package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
class PullRequestCreation {

  private final String sourceRefName;

  private final String targetRefName;

  private final String title;

}
