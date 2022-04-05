package com.societegenerale.cidroid.tasks.consumer.services;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ActionToPerformListener {

    private final ActionToPerformService actionToPerformService;

    private final List<ActionToReplicate> actionsToReplicate;

    private final ActionNotifier notifier;

    private Map<String, Class<? extends ActionToReplicate>> registeredActionsToReplicate;

    public ActionToPerformListener(ActionToPerformService actionToPerformService,
                                   List<ActionToReplicate> actionsToReplicate,
                                   ActionNotifier notifier) {
        this.actionToPerformService = actionToPerformService;
        this.actionsToReplicate = actionsToReplicate;
        this.notifier=notifier;
    }

    @PostConstruct
    protected void registerActionsToReplicate(){

        //since we use class name as key, it's necessarily unique -> there can't be duplicates

        registeredActionsToReplicate = actionsToReplicate.stream().collect(Collectors.toMap(action -> action.getClass().getName(), ActionToReplicate::getClass));

    }


    public void onActionToPerform(ActionToPerformCommand actionToPerformCommand){

        log.info("received an action to perform {}", actionToPerformCommand);

        try{
        Map<String, String> updateActionInfos = (Map) actionToPerformCommand.getUpdateAction();

        String actionToReplicateClass = updateActionInfos.get("@class").trim();

        Class<? extends ActionToReplicate> actionToReplicateToInstantiate = findActualActionToPerform(actionToReplicateClass);

        if (actionToReplicateToInstantiate == null) {
            throw new UnknownActionTypeException(
                "unknown action type " + actionToReplicateClass + ": please check it has been registered correctly");
        }

        ActionToReplicate actionToReplicate = actionToReplicateToInstantiate.getDeclaredConstructor().newInstance();
        actionToReplicate.init(updateActionInfos);

        BulkActionToPerform actionToPerform = BulkActionToPerform.builder()
            .sourceControlPersonalToken(actionToPerformCommand.getSourceControlPersonalToken())
            .email(actionToPerformCommand.getEmail())
            .commitMessage(actionToPerformCommand.getCommitMessage())
            .gitHubInteraction(actionToPerformCommand.getGitHubInteractionType())
            .resourcesToUpdate(actionToPerformCommand.getResourcesToUpdate())
            .actionToReplicate(actionToReplicate)
            .build();

        actionToPerformService.perform(actionToPerform);

    } catch (UnknownActionTypeException e) {
        log.error("can't map the received command to a known action type", e);
        notifyErrorToEndUser(actionToPerformCommand.getEmail(),e);
    } catch (Exception e) {
        log.warn("some unexpected error happened", e);
        notifyErrorToEndUser(actionToPerformCommand.getEmail(),e);
    }

}

    private void notifyErrorToEndUser(String endUserEmail, Exception e){

        User user = new User("unknown user name ", endUserEmail);

        notifier.notify(user,"[KO] unexpected error when request received, before 'core processing' actually happened", "please contact support team to report the issue\n\n"+
            ExceptionUtils.getStackTrace(e));

    }



    private Class<? extends ActionToReplicate> findActualActionToPerform(String actionToReplicateClass) {

        for( Entry<String, Class<? extends ActionToReplicate>> registeredAction : registeredActionsToReplicate.entrySet()){

            if(StringUtils.containsIgnoreCase(actionToReplicateClass,registeredAction.getKey())){
                return registeredAction.getValue();
            }

        }
        return null;
    }

}
