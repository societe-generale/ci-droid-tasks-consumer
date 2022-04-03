package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import java.util.List;

import javax.annotation.Nonnull;

import lombok.Data;

import static java.util.Collections.emptyList;

@Data
public
class OpenPullRequests {

  private List<OpenPullRequest> value;

  @Nonnull
  public List<OpenPullRequest> getValue() {
    return value==null? emptyList() : value;
  }

}
