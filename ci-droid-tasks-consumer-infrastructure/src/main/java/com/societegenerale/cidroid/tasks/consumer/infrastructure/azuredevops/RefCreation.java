package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
class RefCreation {

  private String name;
  private String oldObjectId;
  private String newObjectId;

}
