package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class User {

    private String login;

    private String email;

    public static User buildFrom(PullRequest pr, SourceControlEventsReactionPerformer gitHub){
        return gitHub.fetchUser(pr.getUser().getLogin());
    }

}
