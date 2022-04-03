package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import java.util.List;

import javax.annotation.Nonnull;

import lombok.Builder;
import lombok.Data;

import static java.util.Collections.emptyList;

@Data
@Builder
public
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
