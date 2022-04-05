package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Data;

@Data
public
class PushedCommit {

  private String commitId;
  private String url;

}
