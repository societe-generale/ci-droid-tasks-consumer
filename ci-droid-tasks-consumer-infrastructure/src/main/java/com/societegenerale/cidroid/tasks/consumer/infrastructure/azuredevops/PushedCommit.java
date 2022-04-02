package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Data;

@Data
class PushedCommit {

  private String commitId;
  private String url;

}
