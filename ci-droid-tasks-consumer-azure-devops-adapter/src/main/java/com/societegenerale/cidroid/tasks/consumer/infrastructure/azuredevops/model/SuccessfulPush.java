package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import java.util.List;

import javax.annotation.Nonnull;

import lombok.Data;

import static java.util.Collections.emptyList;

@Data
public
class SuccessfulPush {

  private List<PushedCommit> commits;

  @Nonnull
  public List<PushedCommit> getCommits() {
    return commits==null ? emptyList() : commits;
  }
}
