package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.api.gitHubInteractions.AbstractGitHubInteraction;
import java.util.List;
import javax.annotation.Nonnull;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
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
    private ActionToReplicate updateAction;

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

}
