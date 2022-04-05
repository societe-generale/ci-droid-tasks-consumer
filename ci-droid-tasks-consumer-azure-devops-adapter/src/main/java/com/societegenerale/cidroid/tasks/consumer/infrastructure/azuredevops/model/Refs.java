package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import lombok.Data;

@Data
public
class Refs {

  private List<Ref> value;

  @Nonnull
  public List<Ref> getValue() {
    return value == null ? Collections.emptyList() : value ;
  }


}
