package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import java.util.List;
import lombok.Data;

@Data
class SuccessfulPush {

  private List<PushedCommit> commits;


}
