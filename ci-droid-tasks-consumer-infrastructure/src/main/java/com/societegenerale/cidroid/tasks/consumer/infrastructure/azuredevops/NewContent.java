package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class NewContent {

  private final String content;

  private final String contentType;

}
