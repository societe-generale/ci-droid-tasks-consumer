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

    /**
    public ActionToReplicate getActionToReplicateOutOf(Map<String, Class<? extends ActionToReplicate>> registeredActionsToReplicate)
        throws UnknownActionTypeException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Map<String, String> updateActionInfos = (Map) this.getRawUpdateAction();

        String actionToReplicateClass = updateActionInfos.get("@class").trim();

        Class<? extends ActionToReplicate> actionToReplicateToInstantiate = findActualActionToPerform(actionToReplicateClass,registeredActionsToReplicate);

        if (actionToReplicateToInstantiate == null) {
            throw new UnknownActionTypeException(
                "unknown action type " + actionToReplicateClass + ": please check it has been registered correctly");
        }

        return actionToReplicateToInstantiate.getDeclaredConstructor().newInstance();

    }


    private Class<? extends ActionToReplicate> findActualActionToPerform(String actionToReplicateClass,
                                                    Map<String, Class<? extends ActionToReplicate>> registeredActionsToReplicate) {



        for( String registeredAction : registeredActionsToReplicate.keySet()){

            if(StringUtils.containsIgnoreCase(actionToReplicateClass,registeredAction)){
                return registeredActionsToReplicate.get(registeredAction);
            }

        }

        return null;

    }
**/
}
