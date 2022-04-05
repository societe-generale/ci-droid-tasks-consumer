package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public
class RefCreation {

  private String name;
  private String oldObjectId;
  private String newObjectId;

}
