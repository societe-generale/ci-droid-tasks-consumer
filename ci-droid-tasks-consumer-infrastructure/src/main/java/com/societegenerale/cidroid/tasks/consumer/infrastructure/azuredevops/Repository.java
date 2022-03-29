package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class Repository {

  private String name;

  private String url;

  private String defaultBranch;

}
