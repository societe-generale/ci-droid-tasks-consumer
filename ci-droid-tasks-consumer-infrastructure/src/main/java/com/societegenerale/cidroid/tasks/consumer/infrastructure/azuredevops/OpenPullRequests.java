package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import java.util.List;
import lombok.Data;

@Data
class OpenPullRequests {

  private List<OpenPullRequest> value;

}
