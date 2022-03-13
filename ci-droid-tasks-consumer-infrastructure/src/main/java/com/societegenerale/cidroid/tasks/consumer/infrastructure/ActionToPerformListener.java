package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformService;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ActionToPerformListener {

    private final ActionToPerformService actionToPerformService;

    private final List<ActionToReplicate> actionsToReplicate;

    private final SourceControlBulkActionsPerformer remoteSourceControl;

    private final ActionNotifier notifier;

    private Map<String, Class<? extends ActionToReplicate>> registeredActionsToReplicate;

    public ActionToPerformListener(ActionToPerformService actionToPerformService,
                                   List<ActionToReplicate> actionsToReplicate,
                                   SourceControlBulkActionsPerformer remoteSourceControl,
                                   ActionNotifier notifier) {
        this.actionToPerformService = actionToPerformService;
        this.actionsToReplicate = actionsToReplicate;
        this.remoteSourceControl = remoteSourceControl;
        this.notifier=notifier;
    }

    @PostConstruct
    protected void registerActionsToReplicate(){

        //since we use class name as key, it's necessarily unique -> there can't be duplicates

        registeredActionsToReplicate = actionsToReplicate.stream().collect(Collectors.toMap(action -> action.getClass().getName(), ActionToReplicate::getClass));

    }


    public void onActionToPerform(ActionToPerformCommand actionToPerformCommand) {

        log.info("received an action to perform {}", actionToPerformCommand);

        try {

            if (actionToPerformCommand.getUpdateAction() == null) {
                throw new UnknownActionTypeException("null action type, it has been registered correctly");
            }

            BulkActionToPerform actionToPerform = BulkActionToPerform.builder()
                .sourceControlPersonalToken(actionToPerformCommand.getGitHubOauthToken())
                .email(actionToPerformCommand.getEmail())
                .commitMessage(actionToPerformCommand.getCommitMessage())
                .gitHubInteraction(actionToPerformCommand.getGitHubInteractionType())
                .resourcesToUpdate(actionToPerformCommand.getResourcesToUpdate())
                .actionToReplicate(actionToPerformCommand.getUpdateAction())
                .build();

            actionToPerformService.perform(actionToPerform);

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


}