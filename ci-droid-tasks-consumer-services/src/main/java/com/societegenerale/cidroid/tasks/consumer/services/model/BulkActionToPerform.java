package com.societegenerale.cidroid.tasks.consumer.services.model;

import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.api.gitHubInteractions.AbstractGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "gitHubOauthToken")
@Builder
public class BulkActionToPerform {

    @NotEmpty
    private User userRequestingAction;

    @NotEmpty
    private String gitHubOauthToken;

    @Email
    private String email;

    @NotEmpty
    private String commitMessage;

    @NotNull
    private AbstractGitHubInteraction gitHubInteraction;

    @NotEmpty
    @Nonnull
    private List<ResourceToUpdate> resourcesToUpdate;

    @NotNull
    private ActionToReplicate actionToReplicate;

    public String getActionType() {
        return actionToReplicate.getClass().getName();
    }

}
