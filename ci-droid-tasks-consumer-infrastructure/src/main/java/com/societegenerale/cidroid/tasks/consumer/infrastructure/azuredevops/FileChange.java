package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
class FileChange {

  private final String changeType="edit";

  private final Item item;

  private final NewContent newContent;

}
