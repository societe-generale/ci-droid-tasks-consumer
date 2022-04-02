package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class UpdateRef {

  private final String name;
  private final String oldObjectId ;

}
