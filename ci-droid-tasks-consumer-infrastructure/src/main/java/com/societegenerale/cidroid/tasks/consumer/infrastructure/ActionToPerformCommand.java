package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.AbstractGitHubInteraction;
import java.util.List;
import javax.annotation.Nonnull;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "gitHubOauthToken")
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionToPerformCommand {

    @NotEmpty
    private String gitHubOauthToken;

    @Email
    private String email;

    @NotEmpty
    private String commitMessage;

    @NotNull
    private Object updateAction;

    @NotNull
    private AbstractGitHubInteraction gitHubInteractionType;

    @Nonnull
    public List<ResourceToUpdate> getResourcesToUpdate() {

        if(resourcesToUpdate==null){
            return emptyList();
        }

        return resourcesToUpdate;
    }

    @NotEmpty
    private List<ResourceToUpdate> resourcesToUpdate;
    public ActionToPerformCommand toActionForSingleResource(ResourceToUpdate resourceToUpdate) {

        ActionToPerformCommand action=new ActionToPerformCommand();
        action.setGitHubOauthToken(gitHubOauthToken);
        action.setEmail(email);
        action.setCommitMessage(commitMessage);
        action.setGitHubInteractionType(gitHubInteractionType);
        action.setUpdateAction(updateAction);
        action.setResourcesToUpdate(List.of(resourceToUpdate));

        return action;
    }


}
