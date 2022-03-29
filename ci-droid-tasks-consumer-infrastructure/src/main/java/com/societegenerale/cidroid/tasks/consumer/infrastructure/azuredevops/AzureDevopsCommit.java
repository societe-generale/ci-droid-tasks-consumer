package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import java.util.List;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
class AzureDevopsCommit {

  private final String comment;
  private final List<FileChange> changes;


}
