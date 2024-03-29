package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public
class AzureDevopsRepository {

  private String name;

  private String url;

  private String defaultBranch;

}
