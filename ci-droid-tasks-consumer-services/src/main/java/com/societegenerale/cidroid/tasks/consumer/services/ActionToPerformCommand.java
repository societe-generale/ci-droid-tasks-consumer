package com.societegenerale.cidroid.tasks.consumer.services;

import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.AbstractGitHubInteraction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.emptyList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "sourceControlPersonalToken")
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionToPerformCommand {

    @NotEmpty
    private String sourceControlPersonalToken;

    private String gitLogin;


    /**
     * in previous version, the field 'sourceControlPersonalToken' was called 'gitHubOauthToken', so some clients may relay on this
     * However, we should not use it anymore
     * @param legacyNameForSourceControlPersonalToken
     * @deprecated incoming payloads should not have the field 'gitHubOauthToken' anymore, but 'sourceControlPersonalToken' instead
     */
    @Deprecated
    public void setGitHubOauthToken(String legacyNameForSourceControlPersonalToken) {
        this.sourceControlPersonalToken=legacyNameForSourceControlPersonalToken;
    }


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
        action.setSourceControlPersonalToken(sourceControlPersonalToken);
        action.setEmail(email);
        action.setGitLogin(gitLogin);
        action.setCommitMessage(commitMessage);
        action.setGitHubInteractionType(gitHubInteractionType);
        action.setUpdateAction(updateAction);
        action.setResourcesToUpdate(List.of(resourceToUpdate));

        return action;
    }

}
