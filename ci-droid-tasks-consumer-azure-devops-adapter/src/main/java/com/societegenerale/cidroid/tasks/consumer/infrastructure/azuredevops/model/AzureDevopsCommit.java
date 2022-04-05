package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import java.util.List;

import javax.annotation.Nonnull;

import lombok.Builder;
import lombok.Getter;

import static java.util.Collections.emptyList;

@Builder
@Getter
public class AzureDevopsCommit {

  private final String comment;
  private final List<FileChange> changes;

  @Nonnull
  public List<FileChange> getChanges() {
    return changes==null? emptyList() : changes;
  }
}
