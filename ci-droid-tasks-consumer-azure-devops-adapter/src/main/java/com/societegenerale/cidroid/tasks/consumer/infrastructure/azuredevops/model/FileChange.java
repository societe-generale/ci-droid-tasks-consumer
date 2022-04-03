package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public
class FileChange {

  private final String changeType="edit";

  private final Item item;

  private final NewContent newContent;

}
