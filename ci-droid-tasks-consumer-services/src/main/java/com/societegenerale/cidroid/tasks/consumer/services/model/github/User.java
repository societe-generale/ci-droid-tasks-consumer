package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String login;

    private String email;


    public static User buildFrom(PullRequest pr, RemoteSourceControl gitHub){

        return gitHub.fetchUser(pr.getUser().login);

    }

}
