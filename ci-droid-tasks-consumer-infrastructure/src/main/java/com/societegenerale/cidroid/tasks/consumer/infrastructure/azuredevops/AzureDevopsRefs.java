package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Data;

@Data
public class AzureDevopsRefs {

  private List<AzureDevopsRef> value;

  @Nonnull
  public List<AzureDevopsRef> getValue() {
    return value == null ? Collections.emptyList() : value ;
  }


}
