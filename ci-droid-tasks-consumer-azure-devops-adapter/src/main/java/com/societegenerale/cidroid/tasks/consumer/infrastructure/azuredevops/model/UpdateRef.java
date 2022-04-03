package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public
class UpdateRef {

  private final String name;
  private final String oldObjectId ;

}
