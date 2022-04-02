package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import static java.util.Collections.emptyList;

import java.util.List;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder

class ContentUpdate {

  private List<UpdateRef> refUpdates;

  private List<AzureDevopsCommit> commits;

  @Nonnull
  public List<UpdateRef> getRefUpdates() {
    return refUpdates ==null ? emptyList() : refUpdates ;
  }

  @Nonnull
  public List<AzureDevopsCommit> getCommits() {
    return commits ==null ? emptyList() : commits ;
  }
}
