package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Builder;

@Builder
class PullRequestCreation {

  private final String sourceRefName;

  private final String targetRefName;

  private final String title;

}
